package com.bolsaideas.datajpa.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsaideas.datajpa.models.entity.Cliente;
import com.bolsaideas.datajpa.models.entity.Factura;
import com.bolsaideas.datajpa.models.entity.ItemFactura;
import com.bolsaideas.datajpa.models.entity.Producto;
import com.bolsaideas.datajpa.models.service.IClienteService;

import jakarta.validation.Valid;

@Controller
@Secured("ROLE_ADMIN")
@RequestMapping("/factura")
@SessionAttributes("factura")
public class FacturaController {

	@Autowired
	private IClienteService clienteService;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttribute) {
		Factura factura = clienteService.fetchByIdWithClienteWithItemFacturaWithProducto(id);
		if(factura==null) {
			redirectAttribute.addFlashAttribute("danger", "La Factura no existe en el sistema");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Factura: ".concat(factura.getDescripcion()));
		model.addAttribute("factura", factura);
		return "factura/ver";
	}
	
	@GetMapping("/form/{clienteId}")
	public String crear(@PathVariable(value = "clienteId") Long clienteId, Model model,
			RedirectAttributes redirectAttribute) {
		Cliente cliente = clienteService.findOne(clienteId);
		if (cliente == null) {
			redirectAttribute.addFlashAttribute("danger", "El Cliente no existe en el sistema");
			return "redirect:/listar";
		}
		Factura factura = new Factura();
		factura.setCliente(cliente);
		model.addAttribute("titulo", "Crear Factura");
		model.addAttribute("factura", factura);
		return "factura/form";
	}
	
	@GetMapping(value = "/cargar-productos/{term}", produces = "application/json")
	public @ResponseBody List<Producto> cargarProductos(@PathVariable(value = "term") String term) {
		return clienteService.findByNombre(term);
	}
	
	@PostMapping("/form")
	public String guardar(@Valid Factura factura, BindingResult bindingResult, Model model,
			@RequestParam(name = "detalle_id[]", required = false) Long[] detalleId,
			@RequestParam(name = "cantidad[]", required = false) Integer[] cantidad,
			RedirectAttributes redirectAttribute, SessionStatus sessionStatus) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("titulo", "Crear Factura");
			return "factura/form";
		}

		if (detalleId == null || detalleId.length == 0) {
			model.addAttribute("titulo", "Crear Factura");
			model.addAttribute("danger", "Error: la Factura no puede estar vacia");
			return "factura/form";
		}

		for (int i = 0; i < detalleId.length; i++) {
			Producto producto = clienteService.findProductoById(detalleId[i]);
			ItemFactura itemFactura = new ItemFactura();
			itemFactura.setCantidad(cantidad[i]);
			itemFactura.setProducto(producto);
			factura.addItemFactural(itemFactura);
			logger.info("ItemID: " + detalleId[i].toString() + " , Cantidad: " + cantidad[i].toString());
		}
		clienteService.saveFactura(factura);
		sessionStatus.setComplete();
		redirectAttribute.addFlashAttribute("success", "Factura creada con exito");
		return "redirect:/ver/" + factura.getCliente().getId();
	}
	
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value="id") Long id, RedirectAttributes redirectAttribute) {
		Factura factura = clienteService.findFacturaById(id);
		if(factura != null) {
			clienteService.deleteFactura(id);
			redirectAttribute.addFlashAttribute("success", "Factura eliminada correctamente");
			return "redirect:/ver/" + factura.getCliente().getId();
		}
		redirectAttribute.addFlashAttribute("danger", "Error: La Factura no existe en el sistema");
		return "redirect:/ver/" + clienteService.findFacturaById(id);
	}
}
