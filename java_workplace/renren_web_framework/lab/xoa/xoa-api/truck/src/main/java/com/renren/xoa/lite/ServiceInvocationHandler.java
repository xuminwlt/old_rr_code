package com.renren.xoa.lite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.renren.xoa.XoaClient;
import com.renren.xoa.XoaClientI;
import com.renren.xoa.commons.binaryparam.ParamFormat;
import com.renren.xoa.lite.annotation.XoaDelete;
import com.renren.xoa.lite.annotation.XoaGet;
import com.renren.xoa.lite.annotation.XoaHeader;
import com.renren.xoa.lite.annotation.XoaParam;
import com.renren.xoa.lite.annotation.XoaPost;
import com.renren.xoa.lite.annotation.XoaPut;
import com.renren.xoa.lite.definition.HeaderDefinition;
import com.renren.xoa.lite.definition.MethodDefinition;
import com.renren.xoa.lite.definition.ParamDefinition;
import com.renren.xoa.lite.definition.ServiceDefinition;
import com.renren.xoa.methods.XoaMultiFormatPostMethod;
import com.renren.xoa.util.URIUtil;

/**
 * Xoa方法的Proxy的InvocationHandler
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2010-11-11 下午11:13:56
 */
public class ServiceInvocationHandler implements InvocationHandler{

	protected static Log logger = LogFactory.getLog(ServiceInvocationHandler.class); 

	/**
	 * 存放Method定义
	 */
	private Map<Method, MethodDefinition> methodDefinitions = new ConcurrentHashMap<Method, MethodDefinition>();
	
	private ServiceDefinition serviceDefinition;
	
	private XoaClientI client;
	
	public ServiceInvocationHandler(ServiceDefinition serviceDefinition, XoaClientI client) {
		this.serviceDefinition = serviceDefinition;
		this.client = client;
	}
	
	public ServiceInvocationHandler(ServiceDefinition serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
		client = getXoaClient();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		if (Object.class == method.getDeclaringClass()) {
			return method.invoke(this, args);
        }
		
		MethodDefinition methodDef = methodDefinitions.get(method);	//从缓存中查找
		if (methodDef == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Definition NOT found in cache for " + method.toString());
			}
			methodDef = getMethodDefinition(method);	//分析之
			if (methodDef != null) {
				methodDefinitions.put(method, methodDef);	//放入缓存
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Definition found in cache for " + method.toString());
			}
		}
		if (methodDef == null) {
			throw new IllegalArgumentException("Not XOA method");
		}
		
		Map<String, Object> xoaParams = extractParams(methodDef, args);	//提取参数值
		Map<String, Object> xoaHeaders = extractHeaders(methodDef, args);	//提取header值
		
		com.renren.xoa.Method xoaMethod = createXoaMethod(methodDef, xoaParams, xoaHeaders);
		
		final DefaultServiceFutrue future = new DefaultServiceFutrue(methodDef.getReturnType());
		future.setMethod(xoaMethod);
		future.setXoaClient(client);
		
		return future;
	}
	
	/**
	 * 根据给定信息构造com.renren.xoa.Method
	 * 
	 * @param methodDef
	 * @param xoaParams
	 * @param xoaHeaders
	 * @return
	 */
	@SuppressWarnings("unchecked")
    private com.renren.xoa.Method createXoaMethod(MethodDefinition methodDef, Map<String, Object> xoaParams, Map<String, Object> xoaHeaders) {

		String uri = methodDef.getUri();
		Set<String> hitParams = new HashSet<String>();
		uri = URIUtil.replaceParams(uri, xoaParams, hitParams);	//替换URI模板中的变量
		String url = "xoa://" + serviceDefinition.getServiceId() + uri;
		if (logger.isDebugEnabled()) {
			logger.debug("url:" + url);
		}
		
		boolean multiformat = false;
		//构造Method实例
		com.renren.xoa.Method xoaMethod;
		String sMethod = methodDef.getMethod();
		if ("GET".equalsIgnoreCase(sMethod)) {
			xoaMethod = com.renren.xoa.Method.get(url);
		} else if ("POST".equalsIgnoreCase(sMethod)) {
		    if (XoaPost.CONNTENT_TYPE_MULTIFORMAT.equals(methodDef.getContentType())) {
		        xoaMethod = com.renren.xoa.Method.multiFormatPost(url);
		        multiformat = true;
		    } else {
		        xoaMethod = com.renren.xoa.Method.post(url);
		    }
		} else if ("PUT".equalsIgnoreCase(sMethod)) {
			xoaMethod = com.renren.xoa.Method.put(url);
		} else if ("DELETE".equalsIgnoreCase(sMethod)) {
			xoaMethod = com.renren.xoa.Method.delete(url);
		} else {
			throw new IllegalArgumentException("Illegal xoa method:" + sMethod);
		}
		
		//填参数
		for (Entry<String, Object> entry : xoaParams.entrySet()) {
			String paramName = entry.getKey();
			if (!hitParams.contains(paramName)) {	//已经在URI中出现的参数就不处理了
				
			    ParamDefinition paramDef = methodDef.getParamDefinition(paramName);
			    
			    if (multiformat) {   //multiformat格式的
			        XoaMultiFormatPostMethod multiFormatPostMethod = (XoaMultiFormatPostMethod)xoaMethod;
			        if (XoaParam.TYPE_JAVA.equals(paramDef.getType())) {
			            multiFormatPostMethod.setParam(ParamFormat.JAVA_SERIALIZATION, paramName, entry.getValue());
			        } else {
			            Object value = entry.getValue();
                        if (value instanceof Collection) {
                            Collection<Object> collection = (Collection<Object>)value;
                            for (Object subvalue : collection) {
                                multiFormatPostMethod.setParam(ParamFormat.RAW, paramName, subvalue.toString());
                            }
                        } else if (value.getClass().isArray()) {
                            for (int i = 0; i < Array.getLength(value); i++) {
                                multiFormatPostMethod.setParam(ParamFormat.RAW, paramName, Array.get(value, i).toString());
                            }
                        } else {
                            multiFormatPostMethod.setParam(ParamFormat.RAW, paramName, entry.getValue().toString());
                        }
			        }
			    } else { //普通的raw格式
			        
			        if (XoaParam.TYPE_JSON.equals(paramDef.getType())) { //json类型传输
	                    xoaMethod.setParamAsJson(paramName, entry.getValue());
	                } else {    //普通字符串类型传输
	                    Object value = entry.getValue();
	                    if (value instanceof Collection) {
	                        Collection<Object> collection = (Collection<Object>)value;
	                        for (Object subvalue : collection) {
	                            xoaMethod.setParam(paramName, subvalue.toString());
	                        }
	                    } else if (value.getClass().isArray()) {
	                        for (int i = 0; i < Array.getLength(value); i++) {
	                            xoaMethod.setParam(paramName, Array.get(value, i).toString());
	                        }
	                    } else {
	                        xoaMethod.setParam(paramName, entry.getValue().toString());
	                    }
	                }
			    }
			}
		}

		//填headers
		for (Entry<String, Object> entry : xoaHeaders.entrySet()) {
			xoaMethod.setHeader(entry.getKey(), entry.getValue().toString());
		}
		return xoaMethod;
	}
	
	/**
	 * 提取XOA参数
	 * 
	 * @param methodDef
	 * @param args
	 * @return
	 */
	private Map<String, Object> extractParams(MethodDefinition methodDef, Object[] args) {
		Map<String, Object> xoaParams = new HashMap<String, Object>();
		Iterator<ParamDefinition> iter = methodDef.paramDefinitions();
		while (iter.hasNext()) {
			ParamDefinition paramDef = iter.next();
			xoaParams.put(paramDef.getParamName(), args[paramDef.getParamIndex()]);
		}
		return xoaParams;
	}
	
	/**
	 * 提取XOA header
	 * 
	 * @param methodDef
	 * @param args
	 * @return
	 */
	private Map<String, Object> extractHeaders(MethodDefinition methodDef, Object[] args) {
		Map<String, Object> xoaHeaders = new HashMap<String, Object>();
		Iterator<HeaderDefinition> iter = methodDef.headerDefinitions();
		while (iter.hasNext()) {
			HeaderDefinition headerDef = iter.next();
			xoaHeaders.put(headerDef.getHeaderName(), args[headerDef.getParamIndex()]);
		}
		return xoaHeaders;
	}
	
	/**
	 * 解析方法参数，提取XoaParam的定义和XoaHeader的定义
	 * @param method
	 * @param methodDef
	 */
	private void resolveParamDefinition(Method method, MethodDefinition methodDef) {
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		for (int i = 0; i < paramAnnotations.length; i++) {
			for (int j = 0; j < paramAnnotations[i].length; j++) {
				if (paramAnnotations[i][j] instanceof XoaParam) {
					XoaParam xoaParam = (XoaParam)paramAnnotations[i][j];
					methodDef.addParamDefinition(xoaParam.value(), i, xoaParam.type());
					if (logger.isDebugEnabled()) {
						logger.debug("Found @" + XoaParam.class.getSimpleName()
								+ ": " + xoaParam.value() + " for the " + i + "-th param");
					}
				} else if (paramAnnotations[i][j] instanceof XoaHeader) {
					XoaHeader xoaHeader = (XoaHeader)paramAnnotations[i][j];
					methodDef.addHeaderDefinition(xoaHeader.value(), i);
					if (logger.isDebugEnabled()) {
						logger.debug("Found @" + XoaHeader.class.getSimpleName()
								+ ": " + xoaHeader.value() + " for the " + i + "-th param");
					}
				}
			}
		}
	}
	
	/**
	 * 分析方法的定义，提取XOA相关的信息
	 * 
	 * @param method
	 * @return
	 */
	private MethodDefinition getMethodDefinition(Method method) {
		
		//TODO 看看这段代码能否简化一下
		
		String uri = null;
		String restMethondName = null;
		String contentType = null;
		
		XoaGet xoaget = method.getAnnotation(XoaGet.class);
		if (xoaget != null) {
			uri = xoaget.value();
			restMethondName = "GET";
		}
		
		XoaPost xoapost = method.getAnnotation(XoaPost.class);
		if (xoapost != null) {
			uri = xoapost.value();
			restMethondName = "POST";
			contentType = xoapost.conntentType();
		}
		
		XoaPut xoaput = method.getAnnotation(XoaPut.class);
		if (xoaput != null) {
			uri = xoaput.value();
			restMethondName = "PUT";
		}
		
		XoaDelete xoadelete = method.getAnnotation(XoaDelete.class);
		if (xoadelete != null) {
			uri = xoadelete.value();
			restMethondName = "DELETE";
		}
		
		if (uri == null || restMethondName == null) {
			return null;
		}
		
		//如果uri非空串，那么必须以'/'开头，这是强制的规范要求
		if (uri.length() > 0 && !uri.startsWith("/")) {
			throw new IllegalArgumentException(constructNoHeadingSlashMessage(
					method, restMethondName, uri));
		}

		MethodDefinition def = new MethodDefinition();
		def.setUri(uri);
		def.setMethod(restMethondName);
		def.setContentType(contentType);
		resolveParamDefinition(method, def);
		Type type = method.getGenericReturnType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			def.setReturnType(pType.getActualTypeArguments()[0]);
		} else {
			throw new RuntimeException("Unsupported return type:"
					+ type.getClass() + " " + type.toString() + ". Only "
					+ ServiceFuture.class.getName() + "<T> is supported.");
		}
		return def;
	}
	
	private String constructNoHeadingSlashMessage(Method method,
			String restMethondName, String uri) {
		String ret = "Annotation on ";
		ret += method.getDeclaringClass().getName() + "." + method.getName() + "(...): ";
		ret += getAnnotationNameByMethod(restMethondName) + "(\"" + uri
		+ "\"), "; 
		ret += "URI should be start with '/', that is \"/" + uri + "\"";
		return ret;
	}
	
	private String getAnnotationNameByMethod(String methodName) {
		return "Xoa" + methodName.substring(0, 1).toUpperCase()
				+ methodName.substring(1).toLowerCase();
	}
	
	private static XoaClientI defaultClient = new XoaClient();
	private XoaClientI getXoaClient() {
		return defaultClient;
	}
	
}
