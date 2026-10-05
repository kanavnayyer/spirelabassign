package com.kanav.spirelabassign.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanav.spirelabassign.data.CartRepository
import com.kanav.spirelabassign.data.local.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val itemCount: Int = 0,
    val totalPrice: Double = 0.0
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.items,
        cartRepository.itemCount,
        cartRepository.totalPrice
    ) { items, count, total ->
        CartUiState(items, count, total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartUiState())

    fun increase(productId: Int) = viewModelScope.launch { cartRepository.increase(productId) }

    fun decrease(productId: Int) = viewModelScope.launch { cartRepository.decrease(productId) }

    fun remove(productId: Int) = viewModelScope.launch { cartRepository.remove(productId) }
}
