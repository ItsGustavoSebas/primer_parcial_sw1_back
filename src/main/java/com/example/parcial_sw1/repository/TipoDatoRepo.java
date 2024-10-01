package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Tipo_Dato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoDatoRepo extends JpaRepository<Tipo_Dato, Integer> {
    Optional<Tipo_Dato> findByNombre(String nombre);
}
