package com.example.parcial_sw1.controller;

import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.entity.Proyecto;
import com.example.parcial_sw1.entity.Relacion;
import com.example.parcial_sw1.entity.Role;
import com.example.parcial_sw1.entity.Tabla;
import com.example.parcial_sw1.service.ProyectoService;
import com.example.parcial_sw1.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/proyectos")
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping("get/{proyectoId}")
    public ResponseEntity<Proyecto> getProyecto(@PathVariable int proyectoId) {
        Proyecto proyecto = proyectoService.getProyecto(proyectoId);
        return ResponseEntity.ok(proyecto);
    }

    @GetMapping("")
    public ResponseEntity<List<Proyecto>> getProyectos() {
        List<Proyecto> proyectos = proyectoService.getProyectos();
        return ResponseEntity.ok(proyectos);
    }

    @PostMapping("/crearTabla/{proyectoId}")
    public ResponseEntity<Tabla> crearTabla(@PathVariable int proyectoId, @RequestBody ReqRes reqRes) throws Exception {
        return ResponseEntity.ok(proyectoService.guardarTabla(proyectoId, reqRes));
    }

    @PostMapping("/atributos/{tablaId}")
    public ResponseEntity<Tabla> guardarAtributos(@PathVariable int tablaId, @RequestBody ReqRes reqRes) throws Exception {
        return ResponseEntity.ok(proyectoService.guardarAtributos(tablaId, reqRes));
    }

    @PostMapping("/posicion/{tablaId}")
    public ResponseEntity<Tabla> editarPosicion(@PathVariable int tablaId, @RequestBody ReqRes reqRes) throws Exception {
        return ResponseEntity.ok(proyectoService.editarPosicion(tablaId, reqRes));
    }

    @PostMapping("/relacion")
    public ResponseEntity<Relacion> crearRelacion(@RequestBody ReqRes reqRes) throws Exception {
        return ResponseEntity.ok(proyectoService.guardarRelacion(reqRes));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Proyecto>> getProyectosUser() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<Proyecto> proyectos = proyectoService.obtenerProyectosPorUsuario(email);
        return ResponseEntity.ok(proyectos);
    }

    @PostMapping("/with-file")
    public ResponseEntity<Map<String, Object>> createProjectWithFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion
    ) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        // Llama al servicio para crear el proyecto con archivo
        Integer projectId = proyectoService.createProjectWithFile(file, titulo, descripcion, email);

        // Retorna el ID del proyecto
        Map<String, Object> response = new HashMap<>();
        response.put("projectId", projectId);
        return ResponseEntity.ok(response);
    }

    // Endpoint para crear un proyecto sin archivo
    @PostMapping("/without-file")
    public ResponseEntity<Map<String, Object>> createProjectWithoutFile(
            @RequestBody Map<String, String> projectData
    ) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String titulo = projectData.get("titulo");
        String descripcion = projectData.get("descripcion");

        // Llama al servicio para crear el proyecto sin archivo
        Integer projectId = proyectoService.createProjectWithoutFile(email, titulo, descripcion);

        // Retorna el ID del proyecto
        Map<String, Object> response = new HashMap<>();
        response.put("projectId", projectId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitacion")
    public ResponseEntity<ReqRes> enviarInvitacion(@RequestBody ReqRes req){
        return ResponseEntity.ok(proyectoService.enviarInvitacion(req));
    }

    @DeleteMapping("/delete/{tablaId}")
    public ResponseEntity<ReqRes> deleteTabla(@PathVariable int tablaId){
        return ResponseEntity.ok(proyectoService.deleteTabla(tablaId));
    }

    @DeleteMapping("/deleteRelacion/{relacionId}")
    public ResponseEntity<ReqRes> deleteRelacion(@PathVariable int relacionId){
        return ResponseEntity.ok(proyectoService.deleteRelacion(relacionId));
    }

}
