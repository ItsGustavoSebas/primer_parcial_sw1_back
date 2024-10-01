package com.example.parcial_sw1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "colaborador")
@JsonIgnoreProperties({"proyecto"})
public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDate fecha;
    private String permiso;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private OurUsers usuario;

    @ManyToOne
    @JoinColumn(name = "proyectoId")
    private Proyecto proyecto;

}
