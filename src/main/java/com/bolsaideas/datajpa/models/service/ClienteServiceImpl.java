package com.bolsaideas.datajpa.models.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bolsaideas.datajpa.models.dao.IClienteDao;
import com.bolsaideas.datajpa.models.dao.IFacturaDao;
import com.bolsaideas.datajpa.models.dao.IProductoDao;
import com.bolsaideas.datajpa.models.entity.Cliente;
import com.bolsaideas.datajpa.models.entity.Factura;
import com.bolsaideas.datajpa.models.entity.Producto;

@Service
public class ClienteServiceImpl implements IClienteService {

	@Autowired
	private IClienteDao clienteDao;

	@Autowired
	private IProductoDao productoDao;

	@Autowired
	private IFacturaDao facturaDao;

	@Override
	@Transactional(readOnly = true)
	public List<Cliente> findAll() {
		return (List<Cliente>) clienteDao.findAll();
	}

	@Override
	@Transactional
	public void save(Cliente cliente) {
		clienteDao.save(cliente);
	}

	@Override
	@Transactional(readOnly = true)
	public Cliente findOne(Long id) {
		Optional<Cliente> clienteOptional = clienteDao.findById(id);
		if (clienteOptional.isPresent()) {
			return clienteOptional.get();
		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public void delete(Long id) {
		clienteDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Cliente> findAll(Pageable pageable) {
		return clienteDao.findAll(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Producto> findByNombre(String term) {
		return productoDao.findByNombreLikeIgnoreCase("%" + term + "%");
	}

	@Override
	@Transactional
	public void saveFactura(Factura factura) {
		facturaDao.save(factura);
	}

	@Override
	@Transactional(readOnly = true)
	public Producto findProductoById(Long id) {
		Optional<Producto> productoOptional = productoDao.findById(id);
		if (productoOptional.isPresent()) {
			return productoOptional.get();
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Factura findFacturaById(Long id) {
		Optional<Factura> facturaOptional = facturaDao.findById(id);
		if (facturaOptional.isPresent()) {
			return facturaOptional.get();
		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public void deleteFactura(Long id) {
		facturaDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Factura fetchByIdWithClienteWithItemFacturaWithProducto(Long id) {
		return facturaDao.fetchByIdWithClienteWithItemFacturaWithProducto(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Cliente fetchByIdWithFacturas(Long id) {
		return clienteDao.fetchByIdWithFactura(id);
	}

}
