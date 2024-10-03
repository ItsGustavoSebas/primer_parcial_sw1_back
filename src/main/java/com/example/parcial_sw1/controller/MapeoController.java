package com.example.parcial_sw1.controller;

import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.dto.TablaDto;
import com.example.parcial_sw1.entity.Proyecto;
import com.example.parcial_sw1.service.MapeoService;
import com.example.parcial_sw1.service.XMIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/mapeo")
public class MapeoController {

    @Autowired
    private MapeoService mapeoService;

    @GetMapping("get/{proyectoId}")
    public ResponseEntity<List<TablaDto>> getProyecto(@PathVariable int proyectoId) throws Exception {
        List<TablaDto> proyecto = mapeoService.obtenerTablasMapeo(proyectoId);
        return ResponseEntity.ok(proyecto);
    }

    @DeleteMapping("/delete/{atributoId}")
    public ResponseEntity<ReqRes> deleteAtributo(@PathVariable int atributoId){
        return ResponseEntity.ok(mapeoService.deleteAtributo(atributoId));
    }

    @PostMapping("/normalizar")
    public ResponseEntity<ReqRes> normalizar(@RequestBody ReqRes req){
        return ResponseEntity.ok(mapeoService.normalizar(req));
    }
}
