package com.webapi.application.models.auth;

import lombok.Data;

@Data
public class LoginForm
{
    private String login;
    private String password;
}
