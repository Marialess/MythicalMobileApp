package com.example.mythical.data

sealed class Category(val category: String) {

    object Shirt: Category("shirt")
    object Pants: Category("pants")
    object Shoes: Category("shoes")
    object Accesory: Category("accesory")

}