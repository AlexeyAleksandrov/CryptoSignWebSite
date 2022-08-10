package com.webapi.application.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "users")
@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Status status;
}
