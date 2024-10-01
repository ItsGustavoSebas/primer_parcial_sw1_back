package com.example.parcial_sw1.config;

import com.example.parcial_sw1.entity.Permission;
import com.example.parcial_sw1.entity.Role;
import com.example.parcial_sw1.repository.PermissionRepo;
import com.example.parcial_sw1.repository.RoleRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("RoleSeeder")
public class RoleSeeder implements CommandLineRunner, Ordered {

    @Override
    public int getOrder() {
        return 5;
    }


    @Autowired
    private RoleRepo rolRepository;

    @Autowired
    private PermissionRepo permisoRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Permission permisoListarRoles = new Permission();
        permisoListarRoles.setName("Listar Roles");
        permisoRepository.save(permisoListarRoles);

        Permission permisoActualizarRoles = new Permission();
        permisoActualizarRoles.setName("Actualizar Roles");
        permisoRepository.save(permisoActualizarRoles);

        Permission permisoListarUsuarios = new Permission();
        permisoListarUsuarios.setName("Listar Usuarios");
        permisoRepository.save(permisoListarUsuarios);

        Permission permisoCrearUsuarios = new Permission();
        permisoCrearUsuarios.setName("Crear Usuarios");
        permisoRepository.save(permisoCrearUsuarios);

        Permission permisoEliminarUsuarios = new Permission();
        permisoEliminarUsuarios.setName("Eliminar Usuarios");
        permisoRepository.save(permisoEliminarUsuarios);

        Permission permisoActualizarUsuarios = new Permission();
        permisoActualizarUsuarios.setName("Actualizar Usuarios");
        permisoRepository.save(permisoActualizarUsuarios);


        Role rolAdmin = new Role();
        rolAdmin.setName("ADMIN");
        rolAdmin.setPermissions(Arrays.asList(permisoListarRoles, permisoActualizarRoles,
                permisoListarUsuarios, permisoCrearUsuarios, permisoActualizarUsuarios, permisoEliminarUsuarios));
        rolRepository.save(rolAdmin);

        Role rolUsuario = new Role();
        rolUsuario.setName("USER");
        rolUsuario.setPermissions(Arrays.asList(permisoListarRoles));
        rolRepository.save(rolUsuario);

    }
}