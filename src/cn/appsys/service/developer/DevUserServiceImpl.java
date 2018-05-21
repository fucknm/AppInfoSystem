package cn.appsys.service.developer;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.appsys.dao.devuser.DevUserMapper;
import cn.appsys.pojo.DevUser;

@Service("devUserService")
public class DevUserServiceImpl implements DevUserService{
	
	@Resource
	private DevUserMapper devUserMapper;
	
	@Override
	public DevUser Login(String devCode, String devPassword) {
		return devUserMapper.Login(devCode, devPassword);
	}

}
