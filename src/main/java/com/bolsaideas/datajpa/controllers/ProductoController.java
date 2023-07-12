package com.bolsaideas.datajpa.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsaideas.datajpa.models.entity.Producto;
import com.bolsaideas.datajpa.models.service.IProductoService;
import com.bolsaideas.datajpa.util.paginator.PageRender;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/producto")
@SessionAttributes("producto")
public class ProductoController {

	@Autowired
	IProductoService productoService;
	
	@Secured("ROLE_USER")
	@GetMapping("/listar")
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Producto> productos = productoService.findAll(pageRequest);
		PageRender<Producto> pageRender = new PageRender<>("producto/listar", productos);
		model.addAttribute("titulo", "Listado de productos");
		model.addAttribute("productos", productos);
		model.addAttribute("page", pageRender);
		return "producto/listar";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/form")
	public String crear(Model model) {
		Producto producto = new Producto();
		model.addAttribute("titulo", "Formulario de producto");
		model.addAttribute("producto", producto);
		return "producto/form";
	}
	
	@Secured("ROLE_ADMIN")
	@PostMapping("/form")
	public String guardar(@Valid Producto producto, BindingResult bindingResult, Model model,
	        RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Producto");
			return "producto/form";
		}
		String mensajeFlash = (producto.getId() != null) ? "Producto editado con exito" : "Producto creado con exito";

		productoService.save(producto);
		sessionStatus.setComplete();
		redirectAttributes.addFlashAttribute("success", mensajeFlash);
		return "redirect:/producto/listar";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttributes) {
		Producto producto = null;

		if (id > 0) {
			producto = productoService.findOne(id);
			if (producto == null) {
				redirectAttributes.addFlashAttribute("danger",
						"Error: hubo un error al editar el producto, el producto no existe en el sistema");
				return "redirect:/producto/listar";
			}
		} else {
			redirectAttributes.addFlashAttribute("danger",
					"Error: hubo un error al editar el producto, la ID no puede ser 0");
			return "redirect:/producto/listar";
		}
		model.addAttribute("titulo", "Editar Producto");
		model.addAttribute("producto", producto);
		return "producto/form";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes redirectAttributes) {
		if (id > 0) {
			productoService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Producto eliminado con exito");
		}
		return "redirect:/producto/listar";
	}
}
