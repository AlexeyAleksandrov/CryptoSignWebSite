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
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "firstname")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "passwd")
    private String password;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Status status = Status.ACTIVE;
}
