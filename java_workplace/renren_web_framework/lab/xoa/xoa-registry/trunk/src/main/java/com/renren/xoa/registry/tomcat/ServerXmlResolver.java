package com.renren.xoa.registry.tomcat;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.renren.xoa.registry.XoaServiceDescriptor;
import com.renren.xoa.registry.XoaServiceDescriptorBase;

/**
 * 用于分析tomcat中的server.xml配置文件
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2011-2-28 上午11:46:25
 */
public class ServerXmlResolver {

	protected Log logger = LogFactory.getLog(ServerXmlResolver.class);
	
	/**
	 * 解析server.xml，从中获取当前tomcat实例上运行了哪些服务节点。
	 * 
	 * @param in
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<XoaServiceDescriptor> resolve(InputStream in) {
		
		SAXReader reader = new SAXReader();
		
		int port = -1;
		List<String> hostNames = new ArrayList<String>();
		try {
			Document document = reader.read(in);
			Element root = document.getRootElement().element("Service");
			List<Element> connectorNodes = root.elements("Connector");
			for (Element connectorNode : connectorNodes) {
				String protocol = connectorNode.attributeValue("protocol");
				if (isXoaConnector(protocol)) {
					port = Integer.parseInt(connectorNode.attributeValue("port"));
					if (logger.isInfoEnabled()) {
						logger.info("Found XOA connector on port: " + port);
					}
				}
			}
			
			if (port == -1) {
				logger.error("No XOA Connector found.");
				return null;
			}
			
			List<Element> hostNodes = root.element("Engine").elements("Host");
			if (hostNodes.size() == 0) {
				logger.error("No XOA Host found.");
				return null;
			}
			
			for (Element hostNode : hostNodes) {
				String hostName = hostNode.attributeValue("name");
				hostNames.add(hostName);
			}
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		String localAddress;
		try {
			localAddress = getLocalAddress();
		} catch (UnknownHostException e) {
			logger.error("Can not get local ip address.", e);
			return null;
		}
		
		List<XoaServiceDescriptor> services = new ArrayList<XoaServiceDescriptor>();
		for (String hostName : hostNames) {
			
			XoaServiceDescriptorBase desc = new XoaServiceDescriptorBase();
			desc.setServiceId(hostName);
			desc.setIpAddress(localAddress);
			desc.setPort(port);
			
			services.add(desc);
		}
		
		return services;
	}
	
	private String getLocalAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
	
	
	public boolean isXoaConnector(String protocol) {
		return "com.renren.xoa.server.coyote.NettyHttpProtocolHandler".equals(protocol);
	}
	
}
