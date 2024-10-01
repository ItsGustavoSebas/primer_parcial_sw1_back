package com.example.parcial_sw1.repository;


import com.example.parcial_sw1.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepo extends JpaRepository<OurUsers, Integer> {

    Optional<OurUsers> findByEmail(String email);
    List<OurUsers> findByRoles_Name(String roleName);
    List<OurUsers> findByNameContainingIgnoreCase(String name);
    List<OurUsers> findByRoles_NameAndNameContainingIgnoreCase(String roleName, String name);
    OurUsers findByName(String name);
}
