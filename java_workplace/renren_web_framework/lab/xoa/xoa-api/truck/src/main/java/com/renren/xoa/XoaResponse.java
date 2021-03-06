package com.renren.xoa;

/**
 * 表示一次Xoa调用的返回值
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2010-3-3 下午04:34:09
 */
public interface XoaResponse {

	/**
	 * @return 此次调用的状态码
	 */
	public int getStatusCode();
	
	/**
	 * @return 标识响应此次请求的远程主机
	 */
	public String getRemoteHost();
	
	/**
	 * 返回指定应答头部
	 * @param headerName
	 * @return
	 */
	public String getHeader(String headerName);
	
	/**
	 * 将返回值封装成klass类型的对象并返回
	 * 
	 * @param klass
	 * @return klass类型的对象
	 */
	/**
	 * 将返回值封装成指定的JavaBean对象类型并返回
	 * 
	 * @param <T> JavaBean的类反省
	 * @param klass JavaBean的类
	 * @return 指定的bean对象
	 * @throws ContentParseException 内容解析错误时抛出此异常
	 */
	public <T> T getContentAs(Class<T> klass) throws ContentParseException;
	
	/**
	 * 以String的形式返回内容
	 * @return
	 */
	public String getContentAsString();
}
