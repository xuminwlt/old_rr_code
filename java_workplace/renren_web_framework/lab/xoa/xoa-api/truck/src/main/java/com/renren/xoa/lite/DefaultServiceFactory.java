package com.renren.xoa.lite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.renren.xoa.XoaClientI;
import com.renren.xoa.lite.annotation.XoaService;
import com.renren.xoa.lite.definition.ServiceDefinition;
import com.renren.xoa.util.ClassUtils;


/**
 * {@link ServiceFactory}的默认实现
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2010-11-17 下午12:11:54
 */
public class DefaultServiceFactory implements ServiceFactory {
	
	protected Log logger = LogFactory.getLog(this.getClass());
	
	private Map<Class<?>, ServiceDefinition> xoaServices = new ConcurrentHashMap<Class<?>, ServiceDefinition>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(Class<T> serviceInterface, XoaClientI client) {
		
		ServiceDefinition servicdDef = xoaServices.get(serviceInterface);
		
		if (servicdDef == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Definition NOT found for " + serviceInterface.getName());
			}
			for (Annotation annotation : serviceInterface.getAnnotations()) {
				if (annotation instanceof XoaService) {
					XoaService xoaService = (XoaService)annotation;
					servicdDef = new ServiceDefinition();
					servicdDef.setServiceId(xoaService.serviceId());
					//servicdDef.setHosts(xoaService.hosts());
					xoaServices.put(serviceInterface, servicdDef);
					break;
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Definition found for " + serviceInterface.getName());
				}
			}
		}
		
		if (servicdDef == null) {
			throw new IllegalArgumentException(serviceInterface + " must be annotated with " + XoaService.class);
		}

		ServiceInvocationHandler handler;
		if (client == null) {
			handler = new ServiceInvocationHandler(servicdDef);
		} else {
			handler = new ServiceInvocationHandler(servicdDef, client);
		}
		
		T proxy = (T)Proxy.newProxyInstance(
				ClassUtils.getDefaultClassLoader(), 
				new Class<?>[]{serviceInterface}, handler);
		return proxy;
	}

	@Override
	public <T> T getService(Class<T> serviceInterface) {
		return getService(serviceInterface, null);
	}

}
