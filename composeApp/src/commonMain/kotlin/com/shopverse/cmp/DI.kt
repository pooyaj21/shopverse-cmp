package com.shopverse.cmp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.core.cart.CartManager
import com.shopverse.cmp.core.provider.database.DatabaseProvider
import com.shopverse.cmp.core.provider.dataStore.DataStoreProvider
import com.shopverse.cmp.database.dao.CartItemDAO
import com.shopverse.cmp.network.repository.AuthRepository
import com.shopverse.cmp.network.repository.AuthRepositoryImpl
import com.shopverse.cmp.network.repository.ProductRepository
import com.shopverse.cmp.network.repository.ProductRepositoryImpl
import com.shopverse.cmp.network.service.createHttpClient
import com.shopverse.cmp.network.service.service.AuthService
import com.shopverse.cmp.network.service.service.AuthServiceImpl
import com.shopverse.cmp.network.service.service.ProductService
import com.shopverse.cmp.network.service.service.ProductServiceImpl
import com.shopverse.cmp.network.useCase.GetProductUseCase
import com.shopverse.cmp.network.useCase.GetProductUseCaseImpl
import com.shopverse.cmp.network.useCase.GetProductsUseCase
import com.shopverse.cmp.network.useCase.GetProductsUseCaseImpl
import com.shopverse.cmp.network.useCase.CompleteOnboardingUseCase
import com.shopverse.cmp.network.useCase.CompleteOnboardingUseCaseImpl
import com.shopverse.cmp.network.useCase.IsLoggedInUseCase
import com.shopverse.cmp.network.useCase.IsLoggedInUseCaseImpl
import com.shopverse.cmp.network.useCase.IsOnboardingDoneUseCase
import com.shopverse.cmp.network.useCase.IsOnboardingDoneUseCaseImpl
import com.shopverse.cmp.network.useCase.LoginUseCase
import com.shopverse.cmp.network.useCase.LoginUseCaseImpl
import com.shopverse.cmp.network.useCase.LogoutUseCase
import com.shopverse.cmp.network.useCase.LogoutUseCaseImpl
import com.shopverse.cmp.network.useCase.SignUpUseCase
import com.shopverse.cmp.network.useCase.SignUpUseCaseImpl
import com.shopverse.cmp.screen.cart.CartViewModel
import com.shopverse.cmp.screen.home.HomeViewModel
import com.shopverse.cmp.screen.onboarding.OnboardingViewModel
import com.shopverse.cmp.screen.product.ProductDetailViewModel
import com.shopverse.cmp.screen.splash.SplashViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appKoinModule = module {
    // Platform singletons wired up by the Composable providers in App.kt.
    single<DataStore<Preferences>> { DataStoreProvider.dataStore!! }
    single<CartItemDAO> { DatabaseProvider.cartDao!! }
    single<HttpClient> { createHttpClient(prefs = get()) }
    single { CartManager(cartDao = get()) }

    // Services
    factory<AuthService> { AuthServiceImpl(client = get()) }
    factory<ProductService> { ProductServiceImpl(client = get()) }

    // Repositories
    factory<AuthRepository> { AuthRepositoryImpl(authService = get()) }
    factory<ProductRepository> { ProductRepositoryImpl(productService = get()) }

    // Use cases
    factory<GetProductsUseCase> { GetProductsUseCaseImpl(productRepository = get()) }
    factory<GetProductUseCase> { GetProductUseCaseImpl(productRepository = get()) }
    factory<LoginUseCase> { LoginUseCaseImpl(authRepository = get(), prefs = get()) }
    factory<SignUpUseCase> { SignUpUseCaseImpl(authRepository = get(), prefs = get()) }
    factory<LogoutUseCase> { LogoutUseCaseImpl(authRepository = get(), prefs = get()) }
    factory<IsLoggedInUseCase> { IsLoggedInUseCaseImpl(prefs = get()) }
    factory<IsOnboardingDoneUseCase> { IsOnboardingDoneUseCaseImpl(prefs = get()) }
    factory<CompleteOnboardingUseCase> { CompleteOnboardingUseCaseImpl(prefs = get()) }

    // ViewModels
    viewModelOf(::SplashViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::CartViewModel)
    viewModel { params ->
        ProductDetailViewModel(slug = params.get(), getProduct = get(), cartManager = get())
    }
}
