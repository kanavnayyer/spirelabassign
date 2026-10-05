package com.kanav.spirelabassign.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.kanav.spirelabassign.data.local.CartDao
import com.kanav.spirelabassign.data.local.CartImageStore
import com.kanav.spirelabassign.data.local.CartItem
import com.kanav.spirelabassign.data.remote.ProductDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val cartDao: CartDao,
    private val imageStore: CartImageStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cacheMutex = Mutex()

    val items: Flow<List<CartItem>> = cartDao.observeAll()

    val itemCount: Flow<Int> = items.map { list -> list.sumOf { it.quantity } }

    val totalPrice: Flow<Double> = items.map { list ->
        list.sumOf { it.price * it.quantity }
    }

    init {
        scope.launch { cacheMissingImages() }
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        manager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { cacheMissingImages() }
            }
        })
    }

    fun quantity(productId: Int): Flow<Int> =
        cartDao.observeById(productId).map { it?.quantity ?: 0 }

    suspend fun add(product: ProductDto) {
        val existing = cartDao.getById(product.id)
        val quantity = (existing?.quantity ?: 0) + 1
        val imagePath = cachedPath(existing?.imagePath) ?: imageStore.save(product.id, product.thumbnail)
        cartDao.upsert(
            CartItem(
                productId = product.id,
                title = product.title,
                price = product.price,
                thumbnail = product.thumbnail,
                imagePath = imagePath,
                quantity = quantity
            )
        )
    }

    suspend fun increase(productId: Int) {
        val item = cartDao.getById(productId) ?: return
        cartDao.upsert(item.copy(quantity = item.quantity + 1))
    }

    suspend fun decrease(productId: Int) {
        val item = cartDao.getById(productId) ?: return
        if (item.quantity <= 1) {
            cartDao.delete(productId)
        } else {
            cartDao.upsert(item.copy(quantity = item.quantity - 1))
        }
    }

    suspend fun remove(productId: Int) {
        cartDao.delete(productId)
    }

    suspend fun cacheMissingImages() {
        cacheMutex.withLock {
            cartDao.getAll().forEach { item ->
                if (cachedPath(item.imagePath) != null) return@forEach
                val path = imageStore.save(item.productId, item.thumbnail) ?: return@forEach
                val latest = cartDao.getById(item.productId) ?: return@forEach
                if (latest.imagePath != path) {
                    cartDao.upsert(latest.copy(imagePath = path))
                }
            }
        }
    }

    private fun cachedPath(path: String?): String? = path?.takeIf { imageStore.isCached(it) }
}
