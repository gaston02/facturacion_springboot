package com.bolsaideas.datajpa.models.dao;

import org.springframework.data.repository.CrudRepository;

import com.bolsaideas.datajpa.models.entity.Factura;

public interface IFacturaDao extends CrudRepository<Factura, Long> {

}
