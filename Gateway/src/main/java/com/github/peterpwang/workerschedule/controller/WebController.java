package com.github.peterpwang.workerschedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Web controller class
 * @author Pei Wang
 *
 */
@Controller
public class WebController {

	@RequestMapping(value = "/")
	public String index() {
		return "index";
	}
}