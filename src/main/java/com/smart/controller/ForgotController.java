package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.service.EmailService;
import com.smart.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ForgotController {

	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private Logger logger = LoggerFactory.getLogger( ForgotController.class);

	
	Random random =new Random(1000);
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() { 
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) { 
		System.out.println("EMAIL "+email);
		//logger.info("EMAIL "+email);
		//generating 4 digit otp
		
	int otp =	random.nextInt(999999);
		
	System.out.println("OTP " +otp);
//	logger.info("OTP " +otp);
	//code to send Otp to email..
	String subject="OTP From SCM ";
//	String message= "OTP ="+otp +"";
	String message= ""
			+"<div style='border:1px solid #e2e2e2; padding:20px'>"
			+"<h1>"
			+"OTP is "
			+"<b>" +otp
			+"</b>"
			+"</h1>"
			+"</div>";
	

	String to =email;
	
	boolean flag =this.emailService.sendEmail(subject, message, to);
	if (flag) {
		session.setAttribute("myotp",otp);
		session.setAttribute("email", email);
		return "verify_otp";
	}else{
		session.setAttribute("message","Check your email id !!");
		return "forgot_email_form";
	}
			}
	
	//verify-otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp = (int)session.getAttribute("myotp");
		String email =(String)session.getAttribute("email");
		
		if(myOtp ==otp) {
			//password change form
			User user = this.userRepository.getUserByUserName(email);
			
			if (user==null){
				//send error message
				
				session.setAttribute("message","User does not exits with this email !!");
				return "forgot_email_form";
				
			}else {
				//send change password
				
			}
			
			return "password_change_form";
		}
		
		
		else {
			session.setAttribute("message", "You have entered wrong otp");
			return "verify_otp";
		}
	}
	
	//change-password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword")String newpassword,HttpSession session ) {
		String email =(String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=Your password is successfully changed....";
	}
}
