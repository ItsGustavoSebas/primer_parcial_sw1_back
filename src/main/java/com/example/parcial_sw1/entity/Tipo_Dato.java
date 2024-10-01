package com.example.parcial_sw1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "tipo_dato")
@JsonIgnoreProperties({"atributos"})
public class Tipo_Dato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    @OneToMany(mappedBy = "tipoDato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atributo> atributos;
}
