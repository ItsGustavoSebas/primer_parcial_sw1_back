package com.example.parcial_sw1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "invitacion")
@JsonIgnoreProperties({"proyecto"})
public class Invitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String permiso;
    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;
}
