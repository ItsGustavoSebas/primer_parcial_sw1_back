package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScopeRepo extends JpaRepository<Scope, Integer> {
    Optional<Scope> findByNombre(String nombre);
}
