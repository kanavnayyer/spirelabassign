package com.kanav.spirelabassign.ui.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kanav.spirelabassign.data.remote.ProductDto
import com.kanav.spirelabassign.ui.common.CartAction
import com.kanav.spirelabassign.ui.common.ErrorView
import com.kanav.spirelabassign.ui.common.LoadingView
import com.kanav.spirelabassign.ui.common.QuantityStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    cartCount: Int,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val added by viewModel.added.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(added) {
        if (added) {
            snackbar.showSnackbar("Added to cart")
            viewModel.clearAddedMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { CartAction(cartCount, onCartClick) }
            )
        },
        bottomBar = {
            val qty = quantity
            if (state.product != null && qty != null) {
                CartActionBar(
                    quantity = qty,
                    onAdd = viewModel::addToCart,
                    onIncrease = viewModel::increase,
                    onDecrease = viewModel::decrease
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when {
            state.isLoading -> LoadingView(Modifier.padding(padding))
            state.errorMessage != null -> ErrorView(
                state.errorMessage!!,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding)
            )
            state.product != null -> DetailContent(
                product = state.product!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun CartActionBar(
    quantity: Int,
    onAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (quantity == 0) {
            Button(
                onClick = onAdd,
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Add to Cart")
            }
        } else {
            QuantityStepper(
                quantity = quantity,
                onDecrease = onDecrease,
                onIncrease = onIncrease
            )
        }
    }
}

@Composable
private fun DetailContent(
    product: ProductDto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AsyncImage(
            model = product.thumbnail,
            contentDescription = product.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(16.dp))
        Text(product.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("$${product.price}", style = MaterialTheme.typography.titleLarge)
        Text("★ ${product.rating}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("Category: ${product.category}")
        Text("Brand: ${product.brand ?: "N/A"}")
        Text("Stock: ${product.stock}")
        Spacer(Modifier.height(12.dp))
        Text(product.description, style = MaterialTheme.typography.bodyMedium)
    }
}
