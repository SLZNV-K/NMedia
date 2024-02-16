package ru.netology.nmedia.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class RequestNotificationsPermissionModule {

    @Provides
    fun provideRequest(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}