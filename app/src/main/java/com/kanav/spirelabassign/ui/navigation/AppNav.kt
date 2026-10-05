package com.kanav.spirelabassign.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kanav.spirelabassign.ui.cart.CartScreen
import com.kanav.spirelabassign.ui.cart.CartViewModel
import com.kanav.spirelabassign.ui.products.ProductDetailScreen
import com.kanav.spirelabassign.ui.products.ProductListScreen

object Routes {
    const val PRODUCTS = "products"
    const val DETAIL = "detail/{productId}"
    const val CART = "cart"

    fun detail(id: Int) = "detail/$id"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()
    val cartState by cartViewModel.uiState.collectAsState()

    NavHost(navController, startDestination = Routes.PRODUCTS) {
        composable(Routes.PRODUCTS) {
            ProductListScreen(
                cartCount = cartState.itemCount,
                onProductClick = { navController.navigate(Routes.detail(it)) },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) {
            ProductDetailScreen(
                cartCount = cartState.itemCount,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable(Routes.CART) {
            CartScreen(onBack = { navController.popBackStack() })
        }
    }
}
