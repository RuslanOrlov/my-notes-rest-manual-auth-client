package notes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import notes.props.PropsForCurUser;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
@Controller
public class HomeController {
	
	private PropsForCurUser userProps;
	
	public HomeController(PropsForCurUser userProps) {
		this.userProps = userProps;
	}
	
	@ModelAttribute("isLoggedIn")
	public Boolean isLoggedIn() {
		return this.userProps.isLoggedIn();
	}
	
	@GetMapping("/")
	public String home() {
		return "home";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login-form";
	}
	
}
