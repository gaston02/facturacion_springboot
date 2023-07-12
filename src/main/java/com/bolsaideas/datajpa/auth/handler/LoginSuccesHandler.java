package com.bolsaideas.datajpa.auth.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccesHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		SessionFlashMapManager sessionFlashMapManager = new SessionFlashMapManager();
		FlashMap flashMap = new FlashMap();
		if (authentication != null) {
			flashMap.put("success",
					"Hola ".concat(authentication.getName()).concat(" ha iniciado sesion correctamente"));
			logger.info("El usuario '" + authentication.getName() + "' ha iniciado sesión con éxito");
		}else {
			logger.info("authentication es null");
		}
		sessionFlashMapManager.saveOutputFlashMap(flashMap, request, response);
		super.onAuthenticationSuccess(request, response, authentication);
	}

}
