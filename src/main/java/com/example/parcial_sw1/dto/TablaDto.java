package com.example.parcial_sw1.dto;

import lombok.Data;

import java.util.List;

@Data
public class TablaDto {
    private int id;
    private String nombre;
    private List<AtributoMapDto> atributos;
}
