package com.kanav.spirelabassign.data.remote

data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val brand: String? = null,
    val category: String,
    val thumbnail: String
)

data class ProductsResponse(
    val products: List<ProductDto>,
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)
