package com.kanav.spirelabassign.data

import com.kanav.spirelabassign.data.remote.ProductApi
import com.kanav.spirelabassign.data.remote.ProductDto
import com.kanav.spirelabassign.data.remote.ProductsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val api: ProductApi
) {
    suspend fun getProducts(limit: Int, skip: Int): ProductsResponse =
        api.getProducts(limit, skip)

    suspend fun searchProducts(query: String, limit: Int, skip: Int): ProductsResponse =
        api.searchProducts(query, limit, skip)

    suspend fun getProduct(id: Int): ProductDto = api.getProduct(id)
}
