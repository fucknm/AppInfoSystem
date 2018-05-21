package cn.appsys.controller.backend;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cn.appsys.pojo.BackendUser;
import cn.appsys.service.backend.BackendUserService;
import cn.appsys.tools.Constants;

@Controller
@RequestMapping("manager")
public class UserLoginController {
	
	@Resource
	private BackendUserService backendUserService;
	
	@RequestMapping("login")
	public String login() {
		return "backendlogin";
	}
	
	//后台登录
	@RequestMapping("doLogin")
	public String doLogin(@RequestParam String userCode,@RequestParam String userPassword,HttpServletRequest req) throws Exception {
			BackendUser backendUser = backendUserService.login(userCode, userPassword);
			if (null != backendUser) {
					req.getSession().setAttribute(Constants.USER_SESSION, backendUser);
					return "backend/main";
			} else {
				req.setAttribute("error", "用户名或密码错误！");
				return "index";
			}
	}
	
	//注销用戶
	@RequestMapping("remove")
	public String remove(HttpServletRequest req) {
		req.removeAttribute(Constants.USER_SESSION);
		return "redirect:/manager/login";
	}
}
