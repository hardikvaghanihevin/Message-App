package com.hardik.messageapp.di

import android.content.Context
import com.hardik.messageapp.data.repository.SmsRepositoryImpl
import com.hardik.messageapp.domain.repository.SmsRepository
import com.hardik.messageapp.domain.usecase.GetSmsMessagesUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    fun provideSmsRepository(@ApplicationContext context: Context): SmsRepository { return SmsRepositoryImpl(context) }

    @Provides
    @ViewModelScoped
    fun provideGetSmsMessagesUseCase(smsRepository: SmsRepository): GetSmsMessagesUseCase { return GetSmsMessagesUseCase(smsRepository) }

}
