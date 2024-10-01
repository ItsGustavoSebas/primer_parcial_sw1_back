package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Proyecto;
import com.example.parcial_sw1.entity.Tabla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TablaRepo extends JpaRepository<Tabla, Integer> {
    Optional<Tabla> findByProyectoAndName(Proyecto proyecto, String name);
}
