package com.bolsaideas.datajpa.controllers;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model, Principal principal,
			RedirectAttributes redirectAttribute) {
		if (principal != null) {
			redirectAttribute.addFlashAttribute("info", "Ya ha iniciado sesion anteriormente");
			return "redirect:/";
		}

		if (error != null) {
			model.addAttribute("danger", "Error: Nombre de usuario o contraseña incorrecto");
		}
		
		if(logout != null) {
			model.addAttribute("info", "Ha cerrado sesión correctamente");
		}

		return "login";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttributes) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}
		redirectAttributes.addFlashAttribute("info", "Ha cerrado sesión correctamente");
		return "redirect:/login";
	}

}
