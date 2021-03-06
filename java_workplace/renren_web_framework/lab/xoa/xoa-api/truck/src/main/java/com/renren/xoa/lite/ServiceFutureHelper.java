package com.renren.xoa.lite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.renren.xoa.InvocationInfo;
import com.renren.xoa.StatusNotOkException;
import com.renren.xoa.XoaResponse;
import com.renren.xoa.commons.bean.XoaBizErrorBean;
import com.renren.xoa.commons.exception.XoaException;
import com.renren.xoa.lite.throughput.ThroughputCounter;

/**
 * {@link ServiceFuture}的辅助类，封装一些工具方法，隐藏实现细节
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2011-1-17 下午05:01:46
 */
public class ServiceFutureHelper {

    private static Log logger = LogFactory.getLog(ServiceFutureHelper.class);
    
    private static ThroughputCounter throuputCounter = new ThroughputCounter();
    
	/**
	 * 返回一次future调用的相关信息
	 * @param future
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static String getInvocationInfo(ServiceFuture<?> future) {
		if (future instanceof DefaultServiceFutrue) {
			DefaultServiceFutrue f = (DefaultServiceFutrue)future;
			InvocationInfo info = f.getInvocationInfo();
			if (info != null) {
				StringBuilder sb = new StringBuilder(100);
				sb.append(info.getMethodName());
				sb.append(" ");
				sb.append(info.getUrl());
				sb.append(", remote host: ");
				sb.append(info.getRemoteHost());
				return sb.toString();
			}
		}
		return future.toString();
	}
	
	/**
	 * 如果一个future调用失败了，返回各种情况下的错误信息
	 * 
	 * @param future
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static String getErrorMessage(ServiceFuture<?> future) throws Throwable {
	    if (future.isSuccess()) {
	        throw new IllegalStateException("ServiceFuture not faild: " + future);
	    }
	    
	    StringBuilder sb = new StringBuilder(512);
	    sb.append("Error while invoking XOA:(");
	    
	    if (future instanceof DefaultServiceFutrue) {
            DefaultServiceFutrue f = (DefaultServiceFutrue)future;
            InvocationInfo info = f.getInvocationInfo();
            if (info != null) {
                sb.append(info.getMethodName());
                sb.append(" ");
                sb.append(info.getUrl());
                sb.append(", remote host: ");
                sb.append(info.getRemoteHost());
            }
        } else {
            logger.warn("Unsupported ServiceFuture implementation: " + future.getClass().getName());
        }
	    sb.append("): ");
	    
	    Throwable cause = future.getCause();
        if (cause instanceof StatusNotOkException) {
            StatusNotOkException e = (StatusNotOkException)cause;
            XoaResponse response = e.getResponse();
            int code = response.getStatusCode();
            if (code == XoaBizErrorBean.STATUS_CODE) {
                XoaBizErrorBean error = response.getContentAs(XoaBizErrorBean.class);
                sb.append("errorMsg: ");
                sb.append(error.getMessage());
                if (error.getErrorCode() != 0) {
                    sb.append(", code: ");
                    sb.append(error.getErrorCode());
                }
            } else {
                sb.append("Response status code: ");
                sb.append(code);
                //这种情况下cause.getMessage()返回的信息是重复的，所以就不用再append了
            }
        } else {
            throw cause;
        }
	    return sb.toString();
	}
	
	
	/**
	 * 封装一次xoa调用中重复的逻辑。
	 * 
	 * 传入一个future对象和timeout时间，返回XOA调用的返回值。
	 * 
	 * 如果调用过程中出错或者超时，会抛出一个XoaException e，通过e.getMessage()可以
	 * 获取出错的文本信息。
	 * 
	 * 此方法试用于那些遇到调用出错，只用输出错误log即可的调用需求，如果需要更细粒度的
	 * 出错处理逻辑，请勿使用本方法。
	 * 
	 * @param <T>
	 * @param future
	 * @param timeoutMillis
	 * @return
	 * @throws XoaException
	 */
	public static <T> T execute(ServiceFuture<T> future, long timeoutMillis) throws XoaException {
        try {
            long startTime = System.currentTimeMillis();
            if (future.submit().await(timeoutMillis)) {
                if (future.isSuccess()) {
                    return future.getContent();
                } else {    //response in time but failed
                    throw new XoaException(ServiceFutureHelper.getErrorMessage(future));
                }
            } else {    //timeout
                long endTime = System.currentTimeMillis();
                throw new XoaException("XOA timeout in " + (endTime - startTime) + "ms: "
                        + ServiceFutureHelper.getInvocationInfo(future));
            }
        } catch (Throwable e) {
            throw new XoaException(e);
        }
    }
	
    /**
     * 封装一次xoa调用中重复的逻辑，同时调用的时候提供吞吐量控制。
     * 
     * 在maxThroughput范围内的请求可以最长支持maxTimeout的超时时间，
     * 防止服务端响应速度抖动造成的超时，同时又能保证安全。
     * 
     * @param <T>
     * @param future
     * @param key 用来表示一类调用，同一类调用使用相同的throughput计数来进行控制。
     * 假设你想对某一个接口进行单独的吞吐量控制，可以传入key "GET/user/score"，对其它
     * 接口传入其它key。
     * @param maxThroughput 最大并发的吞吐量
     * @param maxTimeout 在最大吞吐量范围内的最长超时时间
     * @return
     * @throws XoaException
     */
    public static <T> T executeWithThroughputControl(ServiceFuture<T> future, final String key,
            final int maxThroughput, long maxTimeout) throws XoaException {
        
        if (key == null) {
            throw new NullPointerException("Key can not be null.");
        }
        
        int throughput = throuputCounter.incrAndGet(key);
        try {
            if (throughput > maxThroughput) {
                throw new XoaException("Max throughput " + maxThroughput
                        + " exceeded while invoke XOA: "
                        + ServiceFutureHelper.getInvocationInfo(future));
            }
            return execute(future, maxTimeout);
        } finally {
            throuputCounter.decrAndGet(key);
        }
    }
}
