/**
 * 
 */

package com.renren.xoa2.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.renren.xoa2.registry.XoaRegistry;
import com.renren.xoa2.registry.XoaRegistryFactory;
import com.renren.xoa2.registry.impl.NodeImpl;
import com.renren.xoa2.server.conf.Configuration;
import com.renren.xoa2.server.impl.ServiceBuilderImpl;
import com.renren.xoa2.server.impl.XoaService;
import com.renren.xoa2.server.impl.XoaServiceConfig;

/**
 * 服务启动类
 * 启动参数为配置(xoa.xml)文件路径
 * @author jinchao.wen yuan.liu
 * @email  jinchao.wen@opi-corp.com yuan.liu1@renren-inc.com
 * @date   2011-12-14
 */
public class Bootstrap {
    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);    
	
	public final static int MONITOR_TIME_INTERVAL = 100;

	// 服务线程列表
	List<Thread> threadList;
	List<XoaService> serverList;

    public Bootstrap() {
    	threadList = new ArrayList<Thread>();
    	serverList = new ArrayList<XoaService>();
	}
    
	private String getLocalPrivateAddr() {		
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		while (interfaces.hasMoreElements()) {
			NetworkInterface ifc = interfaces.nextElement();
			Enumeration<InetAddress> addressesOfAnInterface = ifc
					.getInetAddresses();

			while (addressesOfAnInterface.hasMoreElements()) {
				InetAddress address = addressesOfAnInterface.nextElement();
				if (address.isSiteLocalAddress()) {
					return address.getHostAddress();
				}
			}
		}
		return "";
    }

    public void startServices(String conf) {
    	Configuration config = Configuration.ConfigurationFactory.newConfiguration(conf);

        XoaRegistry registry = XoaRegistryFactory.getInstance().getDefaultRegistry();
        NodeImpl nodeImpl = new NodeImpl();
        
        IServiceBuilder builder = new ServiceBuilderImpl();
        for (XoaServiceConfig serviceConfig : config.listServiceConfig()) {
            XoaService service = builder.build(serviceConfig);
            Thread curThread = new Thread(service, serviceConfig.getServiceId());
            curThread.start();
            
            // 向registry中注册服务
            String host = getLocalPrivateAddr();
            if (host.isEmpty()) {
            	logger.warn("No private addr available to register the service.");            	
			} else {
				nodeImpl.setHost(host);
				nodeImpl.setPort(serviceConfig.getPort());
				nodeImpl.setDisabled(false);
				boolean registred = registry.registerService(
						serviceConfig.getServiceId(), nodeImpl);

				if (!registred) {
					logger.warn("Service register to zookeeper failed.");
				}
			}
            
            threadList.add(curThread);
            serverList.add(service);
        }
	}
    public void disableServices() {
		XoaRegistry registry = XoaRegistryFactory.getInstance()
				.getDefaultRegistry();
		NodeImpl nodeImpl = new NodeImpl();
		String host = getLocalPrivateAddr();
		nodeImpl.setHost(host);
		nodeImpl.setDisabled(true);
		for (XoaService service : serverList) {
			nodeImpl.setPort(service.getConfig().getPort());
			registry.disableService(service.getConfig().getServiceId(),
					nodeImpl);
		}
	}

    /**
     * 获取内部服务线程的状态
     * 
     * @return 出错服务的数目
     */
    public int getInvalidServicNumber() {
    	int counter = 0;

    	for (Thread thread : threadList) {
    		if (!thread.isAlive()) {
    			++counter;
    			break;
    		}
    	}
    	
    	return counter;
	}

    /**
     * 停止所有内部线程
     * @throws InterruptedException 
     */
    public void haltAll() {
    	for (XoaService service: serverList) {
    		if (service.getServer().isServing()) {
    			service.getServer().stop();
    		}
    		
    		service.getServerTransport().close();
    	}
    	try {
    		Thread.sleep(MONITOR_TIME_INTERVAL);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
	}
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        if( args.length != 1 ){
            printHelp();
            System.exit(1);
        }
        String confFile = args[0];
        
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.startServices(confFile);
        
        /* TODO: 这里有一系列的工作
         * 
         * 1. 主控线程如何在不同异常状态下进行决策 
         * 2. 主控线程如何进行反馈报警工作
         * 3. 主线程在接受到终止信号后，如何结束子线程
         */
        try {
        	while (true) {
        		Thread.currentThread().join(MONITOR_TIME_INTERVAL);
        		
        		if (bootstrap.getInvalidServicNumber() > 0) {
            		// TODO: 当发现内部服务线程异常的情况下，进入处理流程，
        			break;
        		}
        	}
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
			bootstrap.haltAll();
        }
    }
    
    private static void printHelp() {
        System.out.println("MUST specify config file location");
    }
}
