package com.shopverse.cmp.core.provider.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.shopverse.cmp.database.AppDatabase
import com.shopverse.cmp.database.dao.CartItemDAO
import com.shopverse.cmp.database.getShopVerseDatabase

val localCartDao = staticCompositionLocalOf<CartItemDAO> {
    error("No CartItemDAO provided")
}

/** Holds the singletons so DI can inject the DAO into repositories. */
object DatabaseProvider {
    var database: AppDatabase? = null
    var cartDao: CartItemDAO? = null
}

@Composable
fun DatabaseProvider(content: @Composable () -> Unit) {
    if (DatabaseProvider.database == null) {
        DatabaseProvider.database = getShopVerseDatabase()
        DatabaseProvider.cartDao = DatabaseProvider.database?.cartItemDAO()
    }
    CompositionLocalProvider(localCartDao provides DatabaseProvider.cartDao!!) {
        content()
    }
}
