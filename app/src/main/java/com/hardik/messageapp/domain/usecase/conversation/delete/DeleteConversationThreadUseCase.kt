package com.hardik.messageapp.domain.usecase.conversation.delete

import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.model.Message.Companion.toJson
import com.hardik.messageapp.domain.repository.DeleteRepository
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.getOptimalChunkSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DeleteConversationThreadUseCase @Inject constructor(
    private val deleteRepository: DeleteRepository,
    private val messageRepository: MessageRepository,
    private val recyclebinRepository: RecyclebinRepository,
) {
    private val TAG = BASE_TAG + DeleteConversationThreadUseCase::class.java.simpleName
    //suspend operator fun invoke(threadIds: List<Long>): Boolean = deleteRepository.deleteConversationThreads(threadIds)
   /* operator fun invoke(threadIds: List<Long>): Flow<Boolean> = flow {
        // todo : before delete, store all message from each threads and then delete permanently
        val recycleBinThreadEntities = mutableListOf<RecycleBinThreadEntity>()

        messageRepository.getMessagesByThreadIds(threadIds).collect { map ->
            map.forEach { (sender, messages) ->
                if (messages.isNotEmpty()) {
                    val threadId = messages.first().threadId // take the threadId from any message (they all have same)
                    val messageJsonList = messages.toJson()

                    recycleBinThreadEntities.add(
                        RecycleBinThreadEntity(
                            sender = sender,
                            threadId = threadId,
                            messageJson = messageJsonList
                        )
                    )
                }
            }

            // 🔁 Now do both tasks in parallel
            val isDeleted = coroutineScope {
                val moveToRecycleBinDeferred = async { recyclebinRepository.moveToRecycleBinConversationThread(recycleBinThreadEntities) }

                val deleteDeferred = async { deleteRepository.deleteConversationThreads(threadIds) }

                // 🔁 wait for both to complete
                moveToRecycleBinDeferred.await()
                deleteDeferred.await() // emit this
            }

            emit(isDeleted)// Emits the result
        }
    }.flowOn(Dispatchers.IO)*/

    operator fun invoke(threadIds: List<Long>): Flow<Boolean> = flow {
        val chunkSize = getOptimalChunkSize(threadIds.size) // Tune this based on performance/memory tradeoffs
        val threadIdChunks = threadIds.chunked(chunkSize)

        val result = coroutineScope {
            // Launch async tasks for each chunk
            val chunkResults = threadIdChunks.map { chunk ->
                async(Dispatchers.IO) {
                    // todo : before delete, store all message from each threads and then delete permanently
                    val recycleBinThreadEntities = mutableListOf<RecycleBinThreadEntity>()

                    // Collect messages (might return multiple conversations per thread)
                    val messageMap = messageRepository.getMessagesByThreadIds(chunk).first()

                    messageMap.forEach { (sender, messages) ->
                        if (messages.isNotEmpty()) {
                            val threadId = messages.first().threadId
                            val messageJsonList = messages.toJson()

                            recycleBinThreadEntities.add(
                                RecycleBinThreadEntity(
                                    sender = sender,
                                    threadId = threadId,
                                    messageJson = messageJsonList,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    // Perform recycle + delete in parallel for this chunk
                    coroutineScope {
                        val moveDeferred = async { recyclebinRepository.moveToRecycleBinConversationThread(recycleBinThreadEntities) }
                        val deleteDeferred = async { deleteRepository.deleteConversationThreads(chunk) }

                        moveDeferred.await()
                        deleteDeferred.await() // Return delete status
                    }
                }
            }

            // Wait for all chunk jobs
            val allResults = chunkResults.awaitAll()

            // Return true if all deletions were successful
            allResults.all { it }
        }

        emit(result)
    }.flowOn(Dispatchers.IO)

}

