package com.bolsaideas.datajpa.models.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bolsaideas.datajpa.models.entity.Producto;

public interface IProductoService {
	
	public List<Producto> findAll();
	
	public Page<Producto> findAll(Pageable pageable);
	
	public void save(Producto prodcuto);
	
	public Producto findOne(Long id);
	
	public void delete(Long id);
}
