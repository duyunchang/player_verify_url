package com.lance.net.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController {
	/**
	 * 跳转登录页面
	 * @return
	 */
	@RequestMapping(value = {"","/","index","play_verify_url"}, method = RequestMethod.GET)
	public String index(){
		return "index";
	}
	
}