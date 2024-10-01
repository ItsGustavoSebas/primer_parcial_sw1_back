package com.example.parcial_sw1.service;

import com.example.parcial_sw1.entity.Atributo;
import com.example.parcial_sw1.entity.Scope;
import com.example.parcial_sw1.entity.Tabla;
import com.example.parcial_sw1.entity.Tipo_Dato;
import com.example.parcial_sw1.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AtributoService {
    @Autowired
    private AtributoRepo repository;
    @Autowired
    private ScopeRepo scopeRepo;
    @Autowired
    private TipoDatoRepo tipoDatoRepo;
    @Autowired
    private TablaRepo tablaRepo;

    public List<Atributo> findAll() {
        return repository.findAll();
    }

    public Optional<Atributo> findById(int id) {
        return repository.findById(id);
    }

    public Atributo save(Atributo entity) {
        Atributo nuevoAtributo = new Atributo();
        Scope scope = scopeRepo.findById(entity.getScope().getId())
                .orElseThrow(() -> new RuntimeException("Scope no encontrado: " + entity.getScope().getNombre()));
        Tipo_Dato tipoDato = tipoDatoRepo.findById(entity.getTipoDato().getId())
                .orElseThrow(() -> new RuntimeException("Tipo de dato no encontrado: " + entity.getTipoDato().getNombre()));
        Tabla tabla = tablaRepo.findById(entity.getTabla().getId())
                .orElseThrow(() -> new RuntimeException("Tabla no encontrada: " + entity.getTipoDato().getNombre()));
        nuevoAtributo.setNombre(entity.getNombre());
        nuevoAtributo.setNulleable(entity.getNulleable());
        nuevoAtributo.setPk(entity.getPk());
        nuevoAtributo.setTabla(tabla);
        nuevoAtributo.setScope(scope);
        nuevoAtributo.setTipoDato(tipoDato);
        repository.save(nuevoAtributo);
        return repository.save(entity);
    }

    public Atributo edit(Atributo entity, int id) {
        Atributo atributo = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atributo no encontrado: " + id));
        Scope scope = scopeRepo.findById(entity.getScope().getId())
                .orElseThrow(() -> new RuntimeException("Scope no encontrado: " + entity.getScope().getNombre()));
        Tipo_Dato tipoDato = tipoDatoRepo.findById(entity.getTipoDato().getId())
                .orElseThrow(() -> new RuntimeException("Tipo de dato no encontrado: " + entity.getTipoDato().getNombre()));
        Tabla tabla = tablaRepo.findById(entity.getTabla().getId())
                .orElseThrow(() -> new RuntimeException("Tabla no encontrada: " + entity.getTabla().getId()));
        atributo.setNombre(entity.getNombre());
        atributo.setNulleable(entity.getNulleable());
        atributo.setPk(entity.getPk());
        atributo.setTabla(tabla);
        atributo.setScope(scope);
        atributo.setTipoDato(tipoDato);
        repository.save(atributo);
        return repository.save(entity);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }
}
