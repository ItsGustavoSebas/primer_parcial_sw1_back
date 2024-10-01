package com.example.parcial_sw1.controller;

import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.service.ProyectoService;
import com.example.parcial_sw1.service.XMIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/proyectos")
public class XMIController {

    @Autowired
    private XMIService xmiService;

    @PostMapping("/proyectoCompleto")
    public ResponseEntity<Map<String, Object>> crearProyectoConTablasAtributos(@RequestBody ReqRes reqRes) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Integer projectId = xmiService.crearProyectoConTablasAtributos(email, reqRes);
        Map<String, Object> response = new HashMap<>();
        response.put("projectId", projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate/{proyectoId}")
    public ResponseEntity<byte[]> generateXmi(@PathVariable int proyectoId) throws Exception {
        String filePath = xmiService.generarxmi(proyectoId);

        File file = new File(filePath);
        byte[] contents = Files.readAllBytes(file.toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(file.getName(), file.getName());
        return new ResponseEntity<>(contents, headers, HttpStatus.OK);
    }
}
