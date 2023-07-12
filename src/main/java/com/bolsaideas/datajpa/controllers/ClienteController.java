package com.bolsaideas.datajpa.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsaideas.datajpa.models.entity.Cliente;
import com.bolsaideas.datajpa.models.service.IClienteService;
import com.bolsaideas.datajpa.models.service.IUploadFileService;
import com.bolsaideas.datajpa.util.paginator.PageRender;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	@Autowired
	private IClienteService clienteService;

	@Autowired
	private IUploadFileService uploadService;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@GetMapping("/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable(value = "filename") String filename) {
		Resource recurso = null;
		try {
			recurso = uploadService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}
	
	@Secured("ROLE_USER")
	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttributes) {
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (id > 0) {
			if (cliente == null) {
				redirectAttributes.addFlashAttribute("danger",
						"Error: hubo un error al intentar ver el detalle del cliente, el cliente no existe en el sistema");
				return "redirect:/listar";
			}
		} else {
			redirectAttributes.addFlashAttribute("danger",
					"Error: hubo un error al intentar ver el detalle del cliente, la ID no puede ser 0");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Detalle Cliente: " + cliente.getNombre());
		model.addAttribute("cliente", cliente);
		return "ver";
	}

	@GetMapping({ "/listar", "/" })
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Authentication authentication, HttpServletRequest request) {
		if (authentication != null) {
			logger.info("hola usuario autenticado: ".concat(authentication.getName()));
		} else {
			logger.info("authentication es nulo");
		}
		if (hasRole("ROLE_ADMIN")) {
			logger.info("hola ".concat(authentication.getName()).concat(" tienes acceso"));
		} else {
			if (authentication != null && authentication.getName() != null) {
				logger.info("hola ".concat(authentication.getName()).concat(" No tienes acceso"));
			}
		}
		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/form")
	public String crear(Model model) {
		Cliente cliente = new Cliente();
		model.addAttribute("titulo", "Formulario de cliente");
		model.addAttribute("cliente", cliente);
		return "form";
	}
	
	@Secured("ROLE_ADMIN")
	@SuppressWarnings("unused")
	@PostMapping("/form")
	public String guardar(@Valid Cliente cliente, BindingResult bindingResult, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes redirectAttributes,
			SessionStatus sessionStatus) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			return "form";
		}

		if (!foto.isEmpty()) {
			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadService.delete(cliente.getFoto());
			}
			String uniqueFilename = null;
			try {
				uniqueFilename = uploadService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}
			redirectAttributes.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");
			cliente.setFoto(uniqueFilename);
		}

		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con exito" : "Cliente creado con exito";

		clienteService.save(cliente);
		sessionStatus.setComplete();
		redirectAttributes.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttributes) {
		Cliente cliente = null;

		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				redirectAttributes.addFlashAttribute("danger",
						"Error: hubo un error al editar el cliente, el cliente no existe en el sistema");
				return "redirect:/listar";
			}
		} else {
			redirectAttributes.addFlashAttribute("danger",
					"Error: hubo un error al editar el cliente, la ID no puede ser 0");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Editar Cliente");
		model.addAttribute("cliente", cliente);
		return "form";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes redirectAttributes) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);
			clienteService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Cliente eliminado con exito");
			if (cliente.getFoto() != null) {
				if (uploadService.delete(cliente.getFoto())) {
					redirectAttributes.addFlashAttribute("info", "Foto eliminada con exito: " + cliente.getFoto());
				}
			}
		}
		return "redirect:/listar";
	}

	private boolean hasRole(String role) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext == null) {
			return false;
		}
		Authentication authentication = securityContext.getAuthentication();
		if (authentication == null) {
			return false;
		}
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		return authorities.contains(new SimpleGrantedAuthority(role));
	}
}
