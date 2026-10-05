package com.kanav.spirelabassign.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient
) {
    private val saveMutex = Mutex()

    fun isCached(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val file = File(path)
        return file.exists() && file.length() > 0L
    }

    suspend fun save(productId: Int, url: String): String? = saveMutex.withLock {
        withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "cart_images").apply { mkdirs() }
        val file = File(dir, "$productId.img")
        if (file.exists() && file.length() > 0L) return@withContext file.absolutePath

        runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body ?: return@runCatching null
                val tmp = File(dir, "$productId.img.tmp")
                tmp.outputStream().use { output -> body.byteStream().copyTo(output) }
                if (tmp.length() == 0L || !tmp.renameTo(file)) {
                    tmp.delete()
                    return@runCatching null
                }
                file.absolutePath
            }
        }.getOrNull()
        }
    }
}
