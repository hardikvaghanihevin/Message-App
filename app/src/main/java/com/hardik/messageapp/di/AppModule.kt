package com.hardik.messageapp.di

import android.content.Context
import com.hardik.messageapp.data.local.AppDatabase
import com.hardik.messageapp.data.local.dao.ArchivedThreadDao
import com.hardik.messageapp.data.local.dao.BlockThreadDao
import com.hardik.messageapp.data.local.dao.FavoriteMessageDao
import com.hardik.messageapp.data.local.dao.PinThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.repository.ArchiveRepositoryImpl
import com.hardik.messageapp.data.repository.BlockRepositoryImpl
import com.hardik.messageapp.data.repository.ContactRepositoryImpl
import com.hardik.messageapp.data.repository.ConversationRepositoryImpl
import com.hardik.messageapp.data.repository.ConversationThreadRepositoryImpl
import com.hardik.messageapp.data.repository.DeleteRepositoryImpl
import com.hardik.messageapp.data.repository.FavoriteRepositoryImpl
import com.hardik.messageapp.data.repository.MessageRepositoryImpl
import com.hardik.messageapp.data.repository.PinRepositoryImpl
import com.hardik.messageapp.data.repository.RecyclebinRepositoryImpl
import com.hardik.messageapp.data.repository.SearchRepositoryImpl
import com.hardik.messageapp.domain.repository.ArchiveRepository
import com.hardik.messageapp.domain.repository.BlockRepository
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.domain.repository.ConversationRepository
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.domain.repository.DeleteRepository
import com.hardik.messageapp.domain.repository.FavoriteRepository
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.domain.repository.PinRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import com.hardik.messageapp.domain.repository.SearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @EntryPoint
//    @InstallIn(SingletonComponent::class)
//    interface MessageViewModelEntryPoint { fun getMessageViewModel(): MessageViewModel }

//    @Provides
//    @ViewModelScoped
//    fun provideGetSmsMessagesUseCase(smsRepository: MessageRepository): GetMessagesUseCase { return GetMessagesUseCase(smsRepository) }

    //region Provide Util Library class
    @Provides
    @Singleton
    fun providePhoneInstance (@ApplicationContext context: Context): PhoneNumberUtil{
        return PhoneNumberUtil.createInstance(context)
    }
    //endregion

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
    fun provideBlockThreadDao(database: AppDatabase): BlockThreadDao {
        return database.blockThreadDao()
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
                                 recycleBinThreadDao: RecycleBinThreadDao,
                                 blockThreadDao: BlockThreadDao,
                                 conversationThreadRepository: ConversationThreadRepository): ArchiveRepository {
        return ArchiveRepositoryImpl(archivedThreadDao, recycleBinThreadDao, blockThreadDao, conversationThreadRepository)
    }

    @Provides
    @Singleton
    fun provideBlockRepository(@ApplicationContext context: Context, blockThreadDao: BlockThreadDao): BlockRepository {
        return BlockRepositoryImpl(context, blockThreadDao)
    }

    @Provides
    @Singleton
    fun provideContactRepository(@ApplicationContext context: Context): ContactRepository {
        return ContactRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideConversationThreadRepository(@ApplicationContext context: Context, phoneNumberUtil: PhoneNumberUtil, contactRepository: ContactRepository): ConversationThreadRepository {
        return ConversationThreadRepositoryImpl(context, phoneNumberUtil, contactRepository)
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
    fun provideMyDataRepository(@ApplicationContext context: Context, phoneNumberUtil: PhoneNumberUtil): ConversationRepository {
        return ConversationRepositoryImpl(context, phoneNumberUtil)
    }

    @Provides
    @Singleton
    fun providePinRepository(pinThreadDao: PinThreadDao, conversationThreadRepository: ConversationThreadRepository): PinRepository {
        return PinRepositoryImpl(pinThreadDao, conversationThreadRepository)
    }

    @Provides
    @Singleton
    fun provideRecycleBinRepository(@ApplicationContext context: Context,
                                    recycleBinThreadDao: RecycleBinThreadDao,
                                    blockThreadDao: BlockThreadDao,
                                    conversationThreadRepository: ConversationThreadRepository
    ): RecyclebinRepository {
        return RecyclebinRepositoryImpl(context, recycleBinThreadDao, blockThreadDao, conversationThreadRepository)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(@ApplicationContext context: Context, phoneNumberUtil: PhoneNumberUtil): SearchRepository {
        return SearchRepositoryImpl(context, phoneNumberUtil)
    }
    //endregion
}

/*@Module
@InstallIn(ViewModelComponent::class) // ✅ ViewModel Scoped Dependencies
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideBaseViewModelDependencies(
        getConversationUseCase: GetConversationUseCase,
        deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
        getMessagesUseCase: GetMessagesUseCase,
        deleteMessageUseCase: DeleteMessageUseCase,
        insertMessageUseCase: InsertMessageUseCase
    ): BaseViewModelDependencies {
        return BaseViewModelDependencies(
            getConversationUseCase,
            deleteConversationThreadUseCase,
            getMessagesUseCase,
            deleteMessageUseCase,
            insertMessageUseCase
        )
    }
}

@ViewModelScoped
class BaseViewModelDependencies @Inject constructor(
    val getConversationUseCase: GetConversationUseCase,
    val deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
    val getMessagesUseCase: GetMessagesUseCase,
    val deleteMessageUseCase: DeleteMessageUseCase,
    val insertMessageUseCase: InsertMessageUseCase
)*/

