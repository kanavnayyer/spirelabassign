package com.kanav.spirelabassign.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kanav.spirelabassign.data.remote.ProductDto
import com.kanav.spirelabassign.ui.common.CartAction
import com.kanav.spirelabassign.ui.common.EmptyView
import com.kanav.spirelabassign.ui.common.ErrorView
import com.kanav.spirelabassign.ui.common.LoadingView
import com.kanav.spirelabassign.ui.common.ProductImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    cartCount: Int,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                actions = { CartAction(cartCount, onCartClick) }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search products") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> LoadingView(Modifier.weight(1f))
                state.errorMessage != null -> ErrorView(
                    state.errorMessage!!,
                    onRetry = viewModel::retry,
                    modifier = Modifier.weight(1f)
                )
                state.isEmpty -> EmptyView(
                    if (query.isBlank()) "No products found" else "No results for \"$query\"",
                    Modifier.weight(1f)
                )
                else -> ProductList(
                    products = state.products,
                    isLoadingMore = state.isLoadingMore,
                    loadMoreError = state.loadMoreError,
                    onLoadMore = viewModel::loadMore,
                    onProductClick = onProductClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProductList(
    products: List<ProductDto>,
    isLoadingMore: Boolean,
    loadMoreError: String?,
    onLoadMore: () -> Unit,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, products.size, isLoadingMore, loadMoreError) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= products.lastIndex - 2 && products.isNotEmpty()
        }.collect { nearEnd ->
            if (nearEnd && !isLoadingMore && loadMoreError == null) onLoadMore()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductRow(product) { onProductClick(product.id) }
        }
        if (isLoadingMore) {
            item(key = "loader") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (loadMoreError != null) {
            item(key = "load-more-error") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(loadMoreError, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onLoadMore) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: ProductDto, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(product.thumbnail)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text("$${product.price}", style = MaterialTheme.typography.bodyMedium)
                Text("★ ${product.rating}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
