package com.example.parcial_sw1.service;

import com.example.parcial_sw1.entity.Permission;
import com.example.parcial_sw1.repository.PermissionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepo permissionRepo;

    public List<Permission> getPermissions() {
        return permissionRepo.findAll();
    }

}

