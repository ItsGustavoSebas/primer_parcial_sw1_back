package com.example.parcial_sw1.repository;

import com.example.parcial_sw1.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepo extends JpaRepository<Permission, Integer> {
        Permission findById(int id);
}
