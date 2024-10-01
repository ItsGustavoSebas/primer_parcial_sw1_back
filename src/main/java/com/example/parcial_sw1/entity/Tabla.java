package com.example.parcial_sw1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "tabla")
@JsonIgnoreProperties({"proyecto", "relacionesTarget"})
public class Tabla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private Double posicion_x;
    private Double posicion_y;
    private String tabcolor;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @OneToMany(mappedBy = "tablaSource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Relacion> relacionesSource;

    @OneToMany(mappedBy = "tablaTarget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Relacion> relacionesTarget;

    @OneToMany(mappedBy = "tabla", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atributo> atributos;
}
