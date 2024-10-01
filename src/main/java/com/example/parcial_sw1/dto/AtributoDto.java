package com.example.parcial_sw1.dto;

import com.example.parcial_sw1.entity.Scope;
import com.example.parcial_sw1.entity.Tipo_Dato;
import com.example.parcial_sw1.repository.ScopeRepo;
import com.example.parcial_sw1.repository.TipoDatoRepo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
public class AtributoDto {

    private int id;
    private String nombre;
    private boolean nulleable;
    private boolean pk;
    private String scope;
    private String tipoDato;
}
