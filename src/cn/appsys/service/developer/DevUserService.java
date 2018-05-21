package cn.appsys.service.developer;

import cn.appsys.pojo.DevUser;

public interface DevUserService {
	/**
	 * 登录
	 * @param devCode
	 * @return
	 */
	public DevUser Login(String devCode,String devPassword);
}
