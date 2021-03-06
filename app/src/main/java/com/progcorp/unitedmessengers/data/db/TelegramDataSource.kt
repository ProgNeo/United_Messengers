package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramDataSource (private val client: TelegramClient) {

    private fun getConversationIds(limit: Int): Flow<LongArray> =
        callbackFlow {
            client.client?.send(TdApi.GetChats(null, limit)) {
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
            awaitClose { }
        }

    fun loadConversations(limit: Int): Flow<TdApi.Ok> =
        callbackFlow {
            client.client?.send(TdApi.LoadChats(null, limit)) {
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {
                        trySend(it as TdApi.Ok).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("${javaClass.simpleName}.loadConversations", (it as TdApi.Error).message)
                    }
                    else -> {
                        Log.e("${javaClass.simpleName}.loadConversations", "Unknown error")
                    }
                }
            }
            awaitClose { }
        }

    fun getConversations(limit: Int): Flow<List<TdApi.Chat>> =
        getConversationIds(limit)
            .map { ids -> ids.map { getConversation(it) } }
            .flatMapLatest { chatsFlow ->
                combine(chatsFlow) { chats ->
                    chats.toList()
                }
            }

    fun getConversation(chatId: Long): Flow<TdApi.Chat> =
        callbackFlow {
            client.client?.send(TdApi.GetChat(chatId)) {
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

    fun getSupergroup(chatId: Long): Flow<TdApi.Supergroup> =
        callbackFlow {
            client.client?.send(TdApi.GetSupergroup(chatId)) {
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

    fun getBasicGroup(chatId: Long): Flow<TdApi.BasicGroup> =
        callbackFlow {
            client.client?.send(TdApi.GetBasicGroup(chatId)) {
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

    fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<List<TdApi.Message>> =
        callbackFlow {
            client.client?.send(TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)) {
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

    fun getMessage(chatId: Long, messageId: Long): Flow<TdApi.Message?> =
        callbackFlow {
            client.client?.send(TdApi.GetMessage(chatId, messageId)) {
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(it as TdApi.Message).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        trySend(null).isSuccess
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

    fun sendMessage(chatId: Long, message: Message): Flow<TdApi.Message> =
        callbackFlow {
            val text = TdApi.FormattedText((message.content as MessageText).text, arrayOf(TdApi.TextEntity()))
            val input = TdApi.InputMessageText(text, true, false)
            client.client?.send(TdApi.SendMessage(
                chatId,
                0,
                message.replyToMessage?.id ?: 0,
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

    fun markAsRead(chatId: Long, message: Message): Flow<Unit> =
        callbackFlow {
            client.client?.send(TdApi.ViewMessages(
                chatId,
                0,
                longArrayOf(message.id),
                true)){
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {
                        trySend(Unit).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.markAsRead",
                            "${(it as TdApi.Error).message}. Chat ID: $chatId"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.markAsRead",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    fun deleteMessages(chatId: Long, messages: List<Message>, deleteForAll: Boolean): Flow<Unit> =
        callbackFlow {
            val array = LongArray(messages.size) { messages[it].id }
            client.client?.send(TdApi.DeleteMessages(
                chatId,
                array,
                deleteForAll)){
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {
                        trySend(Unit).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.deleteMessages",
                            "${(it as TdApi.Error).message}. Chat ID: $chatId."
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.deleteMessages",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    fun editMessageCaption(chatId: Long, message: Message): Flow<Unit> =
        callbackFlow {
            val text = TdApi.FormattedText(message.content.text, arrayOf(TdApi.TextEntity()))
            client.client?.send(TdApi.EditMessageCaption(
                chatId,
                message.id,
                null,
                text)){
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(Unit).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.editMessageCaption",
                            "${(it as TdApi.Error).message}. Chat ID: $chatId. Message ID: ${message.id}"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.editMessageCaption",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    fun editMessageText(chatId: Long, message: Message): Flow<Unit> =
        callbackFlow {
            val text = TdApi.FormattedText(message.content.text, arrayOf(TdApi.TextEntity()))
            val input = TdApi.InputMessageText(text, true, false)
            client.client?.send(TdApi.EditMessageText(
                chatId,
                message.id,
                null,
                input)){
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(Unit).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e(
                            "${javaClass.simpleName}.editMessageText",
                            "${(it as TdApi.Error).message}. Chat ID: $chatId. Message ID: ${message.id}"
                        )
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.editMessageText",
                            "Unknown error"
                        )
                    }
                }
            }
            awaitClose { }
        }

    fun getUser(userId: Long): Flow<TdApi.User> =
        callbackFlow {
            client.client?.send(TdApi.GetUser(userId)) {
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

    fun getMe(): Flow<TdApi.User> =
        callbackFlow {
            client.client?.send(TdApi.GetMe()) {
                when (it.constructor) {
                    TdApi.User.CONSTRUCTOR -> {
                        trySend(it as TdApi.User).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("${javaClass.simpleName}.getMe", (it as TdApi.Error).message)
                    }
                    else -> {
                        Log.e("${javaClass.simpleName}.getMe", "Unknown error")
                    }
                }
            }
            awaitClose { }
        }

    fun getFile(fileId: Int): Flow<TdApi.File> =
        callbackFlow {
            client.client?.send(TdApi.GetFile(fileId)) {
                when (it.constructor) {
                    TdApi.File.CONSTRUCTOR -> {
                        trySend(it as TdApi.File).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("${javaClass.simpleName}.getFile", (it as TdApi.Error).message)
                    }
                    else -> {
                        Log.e("${javaClass.simpleName}.getFile", "Unknown error")
                    }
                }
            }
            awaitClose { }
        }

    fun downloadFile(fileId: Int): Flow<TdApi.File> =
        callbackFlow {
            client.client?.send(TdApi.DownloadFile(fileId, 1, 0, 0, true)) {
                when (it.constructor) {
                    TdApi.File.CONSTRUCTOR -> {
                        trySend((it as TdApi.File)).isSuccess
                    }
                    else -> {
                        Log.e(
                            "${javaClass.simpleName}.downloadFile",
                            "Unknown error"
                        )
                        cancel("", Exception(""))
                    }
                }
            }
            awaitClose()
        }
}