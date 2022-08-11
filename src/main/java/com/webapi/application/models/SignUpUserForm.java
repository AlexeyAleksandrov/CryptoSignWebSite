package com.webapi.application.models;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class SignUpUserForm
{
    @NotEmpty
    @Size(min = 6, max = 20, message = "Длина логина должна быть от 6 до 20 символов!")
    private String username;

    @NotEmpty
    @Size(min = 6, max = 20, message = "Длина пароля должна быть от 6 до 20 символов!")
    private String password;

    private String confirm_password;

    @AssertTrue(message = "Пароли не совпадают")
    private boolean isPasswordConfirm()
    {
        return password.equals(confirm_password);
    }
}
