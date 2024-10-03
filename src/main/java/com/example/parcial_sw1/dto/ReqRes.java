package com.example.parcial_sw1.dto;

import com.example.parcial_sw1.entity.OurUsers;
import com.example.parcial_sw1.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqRes {

    private int statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String name;
    private String city;
    private List<String> roles;
    private List<String> permissions;
    private String email;
    private String password;
    private OurUsers ourUsers;
    private List<OurUsers> ourUsersList;
    private List<Role> roleList;
    private Role role;
    private String urlAvatar;
    private String titulo;
    private String descripcion;
    private String tipo;
    private String tablaSource;
    private String tablaTarget;

    private Double posicion_x;
    private Double posicion_y;
    private String tabcolor;

    private List<AtributoDto> atributos;

    private Integer tablaSourceId;
    private Integer tablaTargetId;
    private Integer tipoId;
    private String detalle;
    private String multsource;
    private String multtarget;
    private String targetName;
    private String sourceName;
    private String targetArgs;
    private String sourceArgs;

    private List<ReqRes> tablas;

    private List<ReqRes> relaciones;
    private Integer atributoId;
    private Integer tablaId;
    private String permiso;
    private Integer proyectoId;
    private String tipoName;
}
