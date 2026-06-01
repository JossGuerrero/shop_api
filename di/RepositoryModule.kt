// di/RepositoryModule.kt
package com.shopapp.di

import com.shopapp.data.repository.*
import com.shopapp.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository    (impl: AuthRepositoryImpl    ): AuthRepository
    @Binds @Singleton abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Singleton abstract fun bindProductRepository (impl: ProductRepositoryImpl ): ProductRepository
    @Binds @Singleton abstract fun bindOrderRepository   (impl: OrderRepositoryImpl   ): OrderRepository
    @Binds @Singleton abstract fun bindUserRepository    (impl: UserRepositoryImpl    ): UserRepository
}
