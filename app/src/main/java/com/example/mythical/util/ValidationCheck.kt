package com.example.mythical.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation{
    if(email.isEmpty())
        return RegisterValidation.Failed("El campo 'email' no puede estar vacío")

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Formato de email incorrecto")

    return RegisterValidation.Success
}

fun validatePassword(password: String): RegisterValidation{
    if(password.isEmpty())
        return RegisterValidation.Failed("El campo 'Contraseña' no puede estar vacío")

    if (password.length < 6)
        return RegisterValidation.Failed("La contraseña tiene que contener mínimo 6 carácteres")

    return RegisterValidation.Success
}