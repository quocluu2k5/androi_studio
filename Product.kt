package com.example.productmanager.model

data class Product(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = ""
)