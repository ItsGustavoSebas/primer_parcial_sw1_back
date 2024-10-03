package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Atributo;
import com.example.parcial_sw1.entity.Tabla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtributoRepo extends JpaRepository<Atributo, Integer> {
    List<Atributo> findByTabla(Tabla tabla);
}
