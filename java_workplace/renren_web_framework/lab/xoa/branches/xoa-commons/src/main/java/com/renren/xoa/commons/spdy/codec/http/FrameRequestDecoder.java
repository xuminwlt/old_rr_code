package com.renren.xoa.commons.spdy.codec.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.renren.xoa.commons.netty.SpdyHttpRequest;
import com.renren.xoa.commons.spdy.datastructure.ExpireWheel;
import com.renren.xoa.commons.spdy.frame.ControlFrame;
import com.renren.xoa.commons.spdy.frame.DataFrame;
import com.renren.xoa.commons.spdy.frame.Frame;
import com.renren.xoa.commons.spdy.frame.control.RstStream;
import com.renren.xoa.commons.spdy.frame.control.SynReply;
import com.renren.xoa.commons.spdy.frame.control.SynStream;

/**
 * 把Frame对象(串)还原为HttpRequest对象
 * 
 * @author Lookis (lookisliu@gmail.com), Weibo Li
 * 
 */

@ChannelPipelineCoverage("one")
public class FrameRequestDecoder extends SimpleChannelUpstreamHandler {
	
	protected Log logger = LogFactory.getLog(this.getClass());
	
    private ExpireWheel<List<Frame>> frameBuffer = new ExpireWheel<List<Frame>>(
			ExpireWheel.CAPACITY_2P14, 2);
    
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        //TODO:处理disconnected
        super.channelDisconnected(ctx, e);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof Frame) {
            Frame frame = (Frame) e.getMessage();
            
            if (frame instanceof SynStream) {//这是要开启一个数据流呢
                SynStream ss = (SynStream) frame;
                /*int streamId = ss.getStreamID();
                if (frameBuffer.containsKey(streamId)) {//如果已经有了？出错
                    assert (false);
                    //TODO:出错处理
                }*/
                
                if ((ss.getFlags() & ControlFrame.FLAG_FIN) == ControlFrame.FLAG_FIN) {//已经是最后一帧了
                    HttpRequest request = buildRequest(ss, null);
                    //交由下面处理
                    Channels.fireMessageReceived(ctx, request, e.getRemoteAddress());
                    //TODO:处理半关连接
                } else {//不是最后一帧，缓存住
                    //由于收到的是SynStream,必须为一个新的流，所以开新list
                    List<Frame> frameList = new LinkedList<Frame>();
                    frameList.add(ss);
                    frameBuffer.put(ss.getStreamId(), frameList);
                }
            } else if (frame instanceof RstStream) {
                //TODO:连接关闭

            } else if (frame instanceof DataFrame) {
                DataFrame dataFrame = (DataFrame) frame;
				List<Frame> frameList = frameBuffer
						.get(dataFrame.getStreamId());
				if (frameList == null) {
					logger.warn("no frame-list for DataStream:"
							+ dataFrame.getStreamId());
					//有可能是应该由ResponseDecoder处理的DataFrame
					super.messageReceived(ctx, e);
                    return;
				} else {
					frameList.add(dataFrame);
					if ((dataFrame.getFlags() & ControlFrame.FLAG_FIN) == ControlFrame.FLAG_FIN) { // 最后一帧
						SynStream headerFrame = (SynStream) frameList.remove(0);//必须是头帧
						HttpRequest request = buildRequest(headerFrame,
								(List) frameList);
						frameBuffer.remove(headerFrame.getStreamId());
						Channels.fireMessageReceived(ctx, request, e
								.getRemoteAddress());
						// TODO:处理半关连接
					}
				}
                
                /*if (frameBuffer.get(dataFrame.getStreamId()) == null) {
                    //不属于SynStream的DataFrame
                    super.messageReceived(ctx, e);
                    return;
                } else if ((dataFrame.getFlags() & ControlFrame.FLAG_FIN) == 1) {//最后一帧
                    List<Frame> list = frameBuffer.get(dataFrame.getStreamId());
                    SynStream headerFrame = (SynStream) list.get(0);//必须是头帧
                    List<DataFrame> dataList = new LinkedList<DataFrame>();
                    for (Frame frameElement : list) {
                        if (frameElement instanceof DataFrame) {//应该是必须的，这里只检查一下
                            dataList.add((DataFrame) frameElement);
                        }
                    }
                    dataList.add(dataFrame);
                    HttpRequest request = buildRequest(headerFrame, dataList);
                    //清理
                    frameBuffer.remove(headerFrame.getStreamID());
                    Channels.fireMessageReceived(ctx, request, e.getRemoteAddress());
                    //TODO:处理半关连接
                } else {//不是最后一帧，缓存
                    List<Frame> list = frameBuffer.get(dataFrame.getStreamId());
                    list.add(dataFrame);
                }*/
            } else if (frame instanceof SynReply) {
                //这是一个response，略过
                super.messageReceived(ctx, e);
            }
        } else {
            super.messageReceived(ctx, e);
        }
    }

    private SpdyHttpRequest buildRequest(SynStream header, List<DataFrame> dataFrames) {
        Map<String, String> httpHeader = header.getNVMap();
        //version
        HttpVersion httpVersion = httpHeader.get("version").trim().equalsIgnoreCase("HTTP/1.1") ? HttpVersion.HTTP_1_1
                : HttpVersion.HTTP_1_0;
        //method
        HttpMethod httpMethod = null;
        String method = httpHeader.get("method");
        if (method.equalsIgnoreCase("get")) {
            httpMethod = HttpMethod.GET;
        } else if (method.equalsIgnoreCase("post")) {
            httpMethod = HttpMethod.POST;
        } else if (method.equalsIgnoreCase("delete")) {
            httpMethod = HttpMethod.DELETE;
        } else if (method.equalsIgnoreCase("put")) {
            httpMethod = HttpMethod.PUT;
        } else {
        	throw new RuntimeException("Unsupported method:" + method);
        }
        //url
        String uri = httpHeader.get("url");//两边规范的命名不一样
        SpdyHttpRequest request = new SpdyHttpRequest(httpVersion, httpMethod, uri, header
                .getStreamId());
        //set header
        for (Entry<String, String> entry : httpHeader.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        if (dataFrames != null && dataFrames.size() > 0) {//有body
            byte[][] datas = new byte[dataFrames.size()][];
			int offset = 0;
			for (DataFrame dataFrame : dataFrames) {
				datas[offset++] = dataFrame.getData();
			}
			request.setContent(ChannelBuffers.wrappedBuffer(datas));
        }
        return request;
    }
}
