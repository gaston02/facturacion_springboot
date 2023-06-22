package com.bolsaideas.datajpa.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bolsaideas.datajpa.models.entity.Producto;

public interface IProductoDao extends JpaRepository<Producto, Long> {
	
	public List<Producto> findByNombreLikeIgnoreCase(String term);
}
