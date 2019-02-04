package com.github.peterpwang.workerschedule.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.RequestDispatcher;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Web controller class
 * @author Pei Wang
 *
 */
@Controller
public class WebController { //implements ErrorController {

    //private static final String PATH = "/error";

	@RequestMapping(value = "/")
	public String index() {
		return "index";
	}
/*
	@RequestMapping(value = PATH)
	public String error(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
     
		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());
		 
			if(statusCode == HttpStatus.NOT_FOUND.value()) {
				return "error-401";
			}
			else if(statusCode == HttpStatus.FORBIDDEN.value()) {
				return "error-403";
			}
			else if(statusCode == HttpStatus.NOT_FOUND.value()) {
				return "error-404";
			}
			else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				return "error-500";
			}
			
			model.addAttribute("errorCode", statusCode);
		}
		return "error";
	}

    @Override
    public String getErrorPath() {
        return PATH;
    }*/
}