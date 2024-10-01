package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitacionRepo extends JpaRepository<Invitacion, Integer> {
    List<Invitacion> findByEmail(String email);
}
