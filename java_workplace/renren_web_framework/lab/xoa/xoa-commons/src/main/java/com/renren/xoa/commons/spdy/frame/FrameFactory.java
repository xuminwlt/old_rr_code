package com.renren.xoa.commons.spdy.frame;

import com.renren.xoa.commons.exception.MethodNotSupportedException;
import com.renren.xoa.commons.spdy.Utils;
import com.renren.xoa.commons.spdy.frame.control.FrameFactoryHelper;
import com.renren.xoa.commons.spdy.frame.control.RstStream;
import com.renren.xoa.commons.spdy.frame.control.SynReply;
import com.renren.xoa.commons.spdy.frame.control.SynStream;

/**
 * @author Lookis (lookisliu@gmail.com)
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 */
public class FrameFactory {

    private static FrameFactory _instance = new FrameFactory();

    private FrameFactory() {
    }

    public static FrameFactory getInstance() {
        return _instance;
    }
    
    public Frame buildFromByte(byte[] headInByte, byte[] dataInByte, boolean isForProxy) {
        boolean isControlFrame = (headInByte[0] & 0x80) > 0;
        Frame product = null;
        if (isControlFrame) {
            //getVersion
            byte[] versionInByte = Utils.trimBytes(headInByte, 6, 8);
            //修正C位
            versionInByte[0] = (byte) (versionInByte[0] & 0x7F);
            //getType
            byte[] typeInByte = Utils.trimBytes(headInByte, 4, 6);
            int type = Utils.bytesToInt(typeInByte);
            //getFlags
            byte flagsInByte = headInByte[4];
            //build
            switch (type) {
                case 1://SYN_STREAM
                	if (isForProxy){
                		product = FrameFactoryHelper.newSynStreamForProxy(flagsInByte, dataInByte);
                	} else {
                		product = FrameFactoryHelper.newSynStream(flagsInByte, dataInByte);
                	}
                    break;
                case 2://SYN_REPLY
                	if (isForProxy) {
                		product = FrameFactoryHelper.newSynReplyForProxy(flagsInByte, dataInByte);
                	} else {
                		product = FrameFactoryHelper.newSynReply(flagsInByte, dataInByte);
                	}
                    break;
                case 3://RST_STREAM
                    product = new RstStream(flagsInByte, dataInByte);
                    break;
                default:
                    throw new MethodNotSupportedException(
                            "Only SYN_STREAM,SYN_REPLY,RST_STREAM supported!");
            }
        } else {
            //getStreamId,
            //byte[] streamIDInByte = Arrays.copyOfRange(headInByte, 0, 4);
            //不用修正C位，因为DataFrame的C位始终为0
            //streamIDInByte[0] = (byte) (streamIDInByte[0] & 0x7F);
            //int streamID = Utils.bytesToInt(streamIDInByte);
            
            int streamID = Utils.bytesToInt(headInByte, 0, 4);
            //getFlags
            byte flagsInByte = headInByte[4];
            //build
            product = new OptimizedDataFrame(streamID, flagsInByte, dataInByte);
        }
        return product;
    }
    
    public DataFrame newDataFrame(int streamId, byte flags, byte[] data) {
    	return new OptimizedDataFrame(streamId, flags, data);
    }
    
    public SynReply newSynReply(byte flags, int streamId) {
    	return FrameFactoryHelper.newSynReply(flags, streamId);
    }
    
    public SynStream newSynStream(byte flags, int streamId) {
    	return FrameFactoryHelper.newSynStream(flags, streamId);
    }
} 
