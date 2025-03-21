package com.hardik.messageapp.di

import android.content.Context
import com.hardik.messageapp.data.local.AppDatabase
import com.hardik.messageapp.data.local.dao.ArchivedThreadDao
import com.hardik.messageapp.data.local.dao.FavoriteMessageDao
import com.hardik.messageapp.data.local.dao.PinThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.repository.ArchiveRepositoryImpl
import com.hardik.messageapp.data.repository.BlockRepositoryImpl
import com.hardik.messageapp.data.repository.ContactRepositoryImpl
import com.hardik.messageapp.data.repository.ConversationThreadRepositoryImpl
import com.hardik.messageapp.data.repository.DeleteRepositoryImpl
import com.hardik.messageapp.data.repository.FavoriteRepositoryImpl
import com.hardik.messageapp.data.repository.MessageRepositoryImpl
import com.hardik.messageapp.data.repository.PinRepositoryImpl
import com.hardik.messageapp.data.repository.RecyclebinRepositoryImpl
import com.hardik.messageapp.domain.repository.ArchiveRepository
import com.hardik.messageapp.domain.repository.BlockRepository
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.domain.repository.DeleteRepository
import com.hardik.messageapp.domain.repository.FavoriteRepository
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.domain.repository.PinRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @ViewModelScoped
//    fun provideGetSmsMessagesUseCase(smsRepository: MessageRepository): GetMessagesUseCase { return GetMessagesUseCase(smsRepository) }

    //region Provide Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    //endregion

    //region Provide Dao
    @Provides
    @Singleton
    fun provideArchivedThreadDao(database: AppDatabase): ArchivedThreadDao {
        return database.archivedThreadDao()
    }

    @Provides
    @Singleton
    fun provideRecycleBinThreadDao(database: AppDatabase): RecycleBinThreadDao{
        return database.recycleBinThreadDao()
    }

    @Provides
    @Singleton
    fun providePinThreadDao(database: AppDatabase): PinThreadDao {
        return database.pinThreadDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteMessageDao(database: AppDatabase): FavoriteMessageDao {
        return database.favoriteMessageDao()
    }
    //endregion

    //region Provide Repository

    @Provides
    @Singleton
    fun provideArchiveRepository(archivedThreadDao: ArchivedThreadDao,
                                 conversationThreadRepository: ConversationThreadRepository
    ): ArchiveRepository {
        return ArchiveRepositoryImpl(archivedThreadDao, conversationThreadRepository)
    }

    @Provides
    @Singleton
    fun provideBlockRepository(@ApplicationContext context: Context): BlockRepository {
        return BlockRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideContactRepository(@ApplicationContext context: Context): ContactRepository {
        return ContactRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideConversationThreadRepository(@ApplicationContext context: Context, contactRepository: ContactRepository): ConversationThreadRepository {
        return ConversationThreadRepositoryImpl(context, contactRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteRepository(recyclebinRepository: RecyclebinRepository): DeleteRepository {
        return DeleteRepositoryImpl(recyclebinRepository)
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(favoriteMessageDao: FavoriteMessageDao,
                                  messageRepository: MessageRepository
    ): FavoriteRepository {
        return FavoriteRepositoryImpl(favoriteMessageDao, messageRepository)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(@ApplicationContext context: Context): MessageRepository {
        return MessageRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePinRepository(pinThreadDao: PinThreadDao,
                             conversationThreadRepository: ConversationThreadRepository
    ): PinRepository {
        return PinRepositoryImpl(pinThreadDao, conversationThreadRepository)
    }

    @Provides
    @Singleton
    fun provideRecycleBinRepository(@ApplicationContext context: Context,
                                    recycleBinThreadDao: RecycleBinThreadDao,
                                    conversationThreadRepository: ConversationThreadRepository
    ): RecyclebinRepository {
        return RecyclebinRepositoryImpl(context, recycleBinThreadDao, conversationThreadRepository)
    }
    //endregion
}
