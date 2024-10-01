package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.OurUsers;
import com.example.parcial_sw1.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProyectoRepo extends JpaRepository<Proyecto, Integer> {
    List<Proyecto> findByCreador(OurUsers creador);

    @Query("SELECT p FROM Proyecto p JOIN p.colaboradores c WHERE c.usuario = :usuario")
    List<Proyecto> findByColaboradoresContains(@Param("usuario") OurUsers usuario);
}
