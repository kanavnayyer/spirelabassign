package com.kanav.spirelabassign.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanav.spirelabassign.data.ProductRepository
import com.kanav.spirelabassign.data.remote.ProductDto
import com.kanav.spirelabassign.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val products: List<ProductDto> = emptyList(),
    val errorMessage: String? = null,
    val loadMoreError: String? = null,
    val isEmpty: Boolean = false,
    val canLoadMore: Boolean = false
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListState(isLoading = true))
    val uiState: StateFlow<ProductListState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private var searchJob: Job? = null
    private var requestJob: Job? = null

    init {
        fetch(reset = true)
    }

    fun onQueryChange(value: String) {
        _query.value = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            fetch(reset = true)
        }
    }

    fun retry() = fetch(reset = true)

    fun loadMore() = fetch(reset = false)

    private fun fetch(reset: Boolean) {
        val current = _uiState.value
        if (!reset && (current.isLoading || current.isLoadingMore || !current.canLoadMore)) return

        val query = _query.value
        val existing = if (reset) emptyList() else current.products
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.value = current.copy(
                isLoading = reset,
                isLoadingMore = !reset,
                errorMessage = if (reset) null else current.errorMessage,
                loadMoreError = null,
                products = existing,
                isEmpty = false
            )
            try {
                val page = if (query.isBlank()) {
                    repository.getProducts(PAGE_SIZE, existing.size)
                } else {
                    repository.searchProducts(query, PAGE_SIZE, existing.size)
                }
                if (_query.value != query) return@launch
                val merged = existing + page.products
                _uiState.value = ProductListState(
                    isLoading = false,
                    isLoadingMore = false,
                    products = merged,
                    isEmpty = merged.isEmpty(),
                    canLoadMore = page.products.isNotEmpty() && merged.size < page.total
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (_query.value != query) return@launch
                if (reset) {
                    _uiState.value = ProductListState(
                        isLoading = false,
                        errorMessage = error.toUserMessage()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadMoreError = error.toUserMessage(),
                        products = existing
                    )
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
