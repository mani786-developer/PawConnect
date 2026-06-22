package com.example.authapp.di

import com.example.authapp.data.repository.AppointmentRepositoryImpl
import com.example.authapp.data.repository.AuthRepositoryImpl
import com.example.authapp.data.repository.ChatRepositoryImpl
import com.example.authapp.data.repository.PetRepositoryImpl
import com.example.authapp.data.repository.VetRepositoryImpl
import com.example.authapp.domain.repository.AppointmentRepository
import com.example.authapp.domain.repository.AuthRepository
import com.example.authapp.domain.repository.ChatRepository
import com.example.authapp.domain.repository.PetRepository
import com.example.authapp.domain.repository.VetRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Firebase Storage removed — using Cloudinary instead
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository

    @Binds @Singleton
    abstract fun bindVetRepository(impl: VetRepositoryImpl): VetRepository

    @Binds @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}