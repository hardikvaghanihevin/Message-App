package com.hardik.messageapp.domain.usecase.recyclebin

import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import javax.inject.Inject

class MoveToRecyclebinConversationThreadUseCase @Inject constructor(private val recyclebinRepository: RecyclebinRepository){
    suspend operator fun invoke(recycleBinThreads: List<RecycleBinThreadEntity>): Boolean = recyclebinRepository.moveToRecycleBinConversationThread(recycleBinThreads)
}