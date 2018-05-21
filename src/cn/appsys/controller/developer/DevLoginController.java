package cn.appsys.controller.developer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import cn.appsys.pojo.DevUser;
import cn.appsys.service.developer.DevUserService;
import cn.appsys.tools.Constants;

@Controller
@RequestMapping("devuser")
public class DevLoginController {
	
	//DevUser devUser = null;
	
	@Resource
	private DevUserService devUserService;
	
	//页面跳转
	@RequestMapping("devUser")
	public String devUser() {
		return "devlogin";
	}
	
	@RequestMapping("weihu")
	public String weihu() {
		return "developer/appinfolist";
	}
	
	//用户登录
	@RequestMapping("login")
	public String Login(String devCode,String devPassword,HttpServletRequest req) {
		DevUser devUser = devUserService.Login(devCode, devPassword);
		if (null != devUser) {
			req.getSession().setAttribute(Constants.DEV_USER_SESSION, devUser);
			return "main";
		}else {
			req.setAttribute("error", "用户名或密码不正确");
			return "index";
		}
	}
	
	//注销用户
	@RequestMapping("remove")
	public String remove(HttpServletRequest req) {
		req.removeAttribute(Constants.DEV_USER_SESSION);
		return "redirect:/devuser/devUser";
	}
	 
}
