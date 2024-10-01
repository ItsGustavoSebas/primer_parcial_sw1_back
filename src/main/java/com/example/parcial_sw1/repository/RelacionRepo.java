package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Relacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelacionRepo extends JpaRepository<Relacion, Integer> {
    @Query("SELECT r FROM Relacion r WHERE r.tablaSource.proyecto.id = :proyectoId")
    List<Relacion> findAllByProyectoId(@Param("proyectoId") Integer proyectoId);
}
