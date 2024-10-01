package com.example.parcial_sw1.dto;

import lombok.Data;

@Data
public class AtributoMapDto {
    private int id;
    private String nombre;
    private boolean fk;
    private boolean pk;
    private String tipoDato;
}
