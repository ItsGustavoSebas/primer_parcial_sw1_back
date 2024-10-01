package com.example.parcial_sw1.controller;

import com.example.parcial_sw1.entity.Proyecto;
import com.example.parcial_sw1.service.ProyectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/proyectos")
public class PublicController {
    private final ProyectoService proyectoService;

    public PublicController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @GetMapping("get/{proyectoId}")
    public ResponseEntity<Proyecto> getProyecto(@PathVariable int proyectoId) {
        Proyecto proyecto = proyectoService.getProyecto(proyectoId);
        return ResponseEntity.ok(proyecto);
    }
}
