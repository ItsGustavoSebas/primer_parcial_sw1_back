package com.example.parcial_sw1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "atributo")
@JsonIgnoreProperties({"tabla"})
public class Atributo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private Boolean nulleable;
    private Boolean pk;

    @ManyToOne
    @JoinColumn(name = "tabla_id")
    private Tabla tabla;

    @ManyToOne
    @JoinColumn(name = "tipoDato_id")
    private Tipo_Dato tipoDato;

    @ManyToOne
    @JoinColumn(name = "scope_id")
    private Scope scope;
}
