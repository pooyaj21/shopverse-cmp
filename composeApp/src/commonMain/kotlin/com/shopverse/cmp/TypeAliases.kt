package com.shopverse.cmp

import com.shopverse.cmp.model.Product

typealias OnSimpleClick = () -> Unit
typealias OnProductClick = (Product) -> Unit
typealias OnLoginClick = (email: String, password: String) -> Unit
typealias OnSignUpClick = (name: String, email: String, password: String) -> Unit
