package com.renren.xoa.methods;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.renren.xoa.commons.binaryparam.MultiFormatParamEncoder;
import com.renren.xoa.commons.binaryparam.ParamFormat;
import com.renren.xoa.commons.binaryparam.ParameterWrapper;
import com.renren.xoa.commons.binaryparam.impl.ParameterWrapperImpl;

/**
 * 支持多格式参数的Method实现
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2011-5-26 下午12:24:55
 */
public class XoaMultiFormatPostMethod extends XoaPostMethod {
    
    @SuppressWarnings("unchecked")
    private Map<String, ParameterWrapper> multiFormatParams = Collections.EMPTY_MAP;
    
    public void setParam(ParamFormat format, String key, Object value) {
        if (multiFormatParams == Collections.EMPTY_MAP) {
            multiFormatParams = new HashMap<String, ParameterWrapper>();
        }

        ParameterWrapper wrapper = multiFormatParams.get(key);
        if (wrapper == null) {
            ParameterWrapperImpl newWrapper = new ParameterWrapperImpl();
            newWrapper.setFormat(format);
            newWrapper.setKey(key);
            newWrapper.addValue(value);
            multiFormatParams.put(key, newWrapper);
        } else {
            wrapper.addValue(value);
        }
    }
    
    @Override
    public byte[] getBody() {
        if (multiFormatParams == null || multiFormatParams == Collections.EMPTY_MAP) {
            return super.getBody();
        }

        //merge the form parameters
        if (this.parameters != null) {
            
            for (Entry<String, String[]> entry : this.parameters.entrySet()) {
                for (String value : entry.getValue()) {
                    setParam(ParamFormat.RAW, entry.getKey(), value);
                }
            }
        }
        
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        MultiFormatParamEncoder encoder = new MultiFormatParamEncoder();
        try {
            encoder.encodeToStream(multiFormatParams, os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
