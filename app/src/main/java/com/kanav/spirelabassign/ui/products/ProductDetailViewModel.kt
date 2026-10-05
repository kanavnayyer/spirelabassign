package com.kanav.spirelabassign.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanav.spirelabassign.data.CartRepository
import com.kanav.spirelabassign.data.ProductRepository
import com.kanav.spirelabassign.data.remote.ProductDto
import com.kanav.spirelabassign.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailState(
    val isLoading: Boolean = true,
    val product: ProductDto? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailState())
    val uiState: StateFlow<ProductDetailState> = _uiState.asStateFlow()

    private val _added = MutableStateFlow(false)
    val added: StateFlow<Boolean> = _added.asStateFlow()

    val quantity: StateFlow<Int?> = cartRepository.quantity(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProductDetailState(isLoading = true)
            runCatching { productRepository.getProduct(productId) }
                .onSuccess {
                    _uiState.value = ProductDetailState(isLoading = false, product = it)
                }
                .onFailure {
                    _uiState.value = ProductDetailState(
                        isLoading = false,
                        errorMessage = it.toUserMessage()
                    )
                }
        }
    }

    fun addToCart() {
        val product = uiState.value.product ?: return
        viewModelScope.launch {
            val wasEmpty = (quantity.value ?: 0) == 0
            cartRepository.add(product)
            if (wasEmpty) _added.value = true
        }
    }

    fun increase() = addToCart()

    fun decrease() {
        viewModelScope.launch { cartRepository.decrease(productId) }
    }

    fun clearAddedMessage() {
        _added.value = false
    }
}
