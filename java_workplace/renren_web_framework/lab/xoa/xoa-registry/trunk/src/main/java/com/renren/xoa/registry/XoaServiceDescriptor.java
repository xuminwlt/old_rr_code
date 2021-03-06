package com.renren.xoa.registry;

import java.util.Comparator;

/**
 * Xoa服务描述符，用以表示提供服务的真实机器的IP地址和端口
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2010-3-19 下午04:02:16
 */
public interface XoaServiceDescriptor {
	
	public static final String STATUS_ENABLED = "enabled";
	
	public static final String STATUS_DISABLED = "disabled";
	
	/**
	 * @return 服务器的ip地址
	 */
	public String getIpAddress();
	
	/**
	 * @return 要访问的服务的ID
	 */
	public String getServiceId();
	
	/**
	 * @return 服务端口
	 */
	public int getPort();
	
	/**
	 * @return 是否被置为不可用状态
	 */
	public boolean isDisabled();
	
	public static final Comparator<XoaServiceDescriptor> COMPARATOR = new Comparator<XoaServiceDescriptor>() {

		@Override
		public int compare(XoaServiceDescriptor o1, XoaServiceDescriptor o2) {
			
			//先比较serviceId
			int cmp = o1.getServiceId().compareTo(o2.getServiceId());
			if (cmp == 0) {	//serviceId相同
				
				//再比IP地址
				cmp = o1.getIpAddress().compareTo(o2.getIpAddress());
				if (cmp == 0) {	//ip也相同
					
					//最后比端口, 因为端口是正整数，且最大为65545，所以直接减不用担心下溢
					return o1.getPort() - o2.getPort();
				} else {
					return cmp;
				}
			} else {
				return cmp;
			}
		}
	};
	
}
