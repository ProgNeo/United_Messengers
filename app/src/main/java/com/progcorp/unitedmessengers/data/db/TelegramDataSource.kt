package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.TelegramClient
import com.progcorp.unitedmessengers.data.model.Message
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramDataSource @Inject constructor(private val client: TelegramClient) {

    suspend fun getConversationIds(limit: Int): Flow<LongArray> =
        callbackFlow {
            client.client.send(TdApi.GetChats(TdApi.ChatListMain(), limit)) {
                when (it.constructor) {
                    TdApi.Chats.CONSTRUCTOR -> {
                        trySend((it as TdApi.Chats).chatIds).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("${javaClass.simpleName}.getConversations", (it as TdApi.Error).message)
                    }
                    else -> {
                        Log.e("${javaClass.simpleName}.getConversations", "Unknown error")
                    }
                }
            }
            awaitClose {  }
        }

    suspend fun getConversations(limit: Int): Flow<List<TdApi.Chat>> =
        getConversationIds(limit)
            .map { ids -> ids.map { getConversation(it) } }
            .flatMapLatest { chatsFlow -> combine(chatsFlow) { chats -> chats.toList() } }

    suspend fun getConversation(chatId: Long): Flow<TdApi.Chat> =
        callbackFlow {
            client.client.send(TdApi.GetChat(chatId)) {
                when (it.constructor) {
                    TdApi.Chat.CONSTRUCTOR -> {
                        trySend(it as TdApi.Chat).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("${javaClass.simpleName}.getChat", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        Log.e("${javaClass.simpleName}.getChat", "Unknown error")
                    }
                }
            }
            awaitClose { }
        }

    suspend fun getSupergroup(chatId: Long): Flow<TdApi.Supergroup> =
        callbackFlow {
            client.client.send(TdApi.GetSupergroup(chatId)) {
                when (it.constructor) {
                    TdApi.Supergroup.CONSTRUCTOR -> {
                        trySend(it as TdApi.Supergroup).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.getSupergroup",
                            "${(it as TdApi.Error).message}. ID: $chatId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.getSupergroup",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    suspend fun getBasicGroup(chatId: Long): Flow<TdApi.BasicGroup> =
        callbackFlow {
            client.client.send(TdApi.GetBasicGroup(chatId)) {
                when (it.constructor) {
                    TdApi.BasicGroup.CONSTRUCTOR -> {
                        trySend(it as TdApi.BasicGroup).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.getBasicGroup",
                            "${(it as TdApi.Error).message}. ID: $chatId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.getBasicGroup",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }


    suspend fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<List<TdApi.Message>> =
        callbackFlow {
            client.client.send(TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)) {
                when (it.constructor) {
                    TdApi.Messages.CONSTRUCTOR -> {
                        trySend((it as TdApi.Messages).messages.toList()).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.getMessages",
                            "${(it as TdApi.Error).message}. ID: $chatId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.getMessages",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    suspend fun getMessage(chatId: Long, messageId: Long): Flow<TdApi.Message> =
        callbackFlow {
            client.client.send(TdApi.GetMessage(chatId, messageId)) {
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(it as TdApi.Message).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.getMessage",
                            "${(it as TdApi.Error).message}. ID: $messageId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.getMessage",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    suspend fun sendMessage(chatId: Long, message: Message): Flow<TdApi.Message> =
        callbackFlow {
            val text = TdApi.FormattedText(message.text, arrayOf(TdApi.TextEntity()))
            val input = TdApi.InputMessageText(text, true, false)
            client.client.send(TdApi.SendMessage(
                chatId,
                0,
                0,
                null,
                null,
                input)) {
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(it as TdApi.Message).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.sendMessage",
                            "${(it as TdApi.Error).message}. Chat ID: $chatId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.sendMessage",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    suspend fun getUser(userId: Long): Flow<TdApi.User> =
        callbackFlow {
            client.client.send(TdApi.GetUser(userId)) {
                when (it.constructor) {
                    TdApi.User.CONSTRUCTOR -> {
                        trySend((it as TdApi.User)).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.getUser",
                            "${(it as TdApi.Error).message}. ID: $userId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.getUser",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }
}