package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoRepo extends JpaRepository<Tipo, Integer> {
    Optional<Tipo> findByNombre(String nombre);
}
