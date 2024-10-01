package com.example.parcial_sw1.controller;

import com.example.parcial_sw1.entity.Role;
import com.example.parcial_sw1.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<String> updateRolePermissions(@PathVariable int roleId, @RequestBody List<Integer> permissionIds) {
        roleService.updateRolePermissions(roleId, permissionIds);
        return ResponseEntity.ok("Permissions updated successfully for role with ID: " + roleId);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRole(@PathVariable int roleId) {
        Role role = roleService.getRole(roleId);
        return ResponseEntity.ok(role);
    }

    @GetMapping("")
    public ResponseEntity<List<Role>> getRoles() {
        List<Role> roles = roleService.getRoles();
        return ResponseEntity.ok(roles);
    }
}
