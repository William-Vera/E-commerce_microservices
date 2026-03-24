package com.cellc.productservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "especificaciones")
@Getter @Setter
public class Especificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pantalla;
    private String procesador;
    private String ram;
    private String almacenamiento;
    private String bateria;
    private String camara;
    private String sistemaOperativo;

    private Long productoId;
}
