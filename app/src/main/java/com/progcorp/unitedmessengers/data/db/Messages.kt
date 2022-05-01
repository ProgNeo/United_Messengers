package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.vk.requests.VKMessagesRequest
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject

class Messages(private val onMessagesFetched: OnMessagesFetched) {
    fun vkGetMessages(chat: Conversation, offset: Int, count: Int, isNew: Boolean) = runBlocking {
        val response = App.application.vkRetrofit.create(VKMessagesRequest::class.java)
            .messagesGet(
                App.application.vkAccountService.token!!,
                "5.131",
                count,
                offset,
                chat.id,
                true,
                0
            )
        val responseJson = JSONObject(response)
        try {
            val o = responseJson.getJSONObject("response").getJSONArray("items")
            val p = responseJson.getJSONObject("response").optJSONArray("profiles")
            val r = ArrayList<Message>(o.length())
            for (i in 0 until o.length()) {
                val message = Message.parseVK(
                    o.getJSONObject(i),
                    p
                )
                r.add(message)
            }
            onMessagesFetched.showMessages(r, isNew)
        } catch (ex: JSONException) {
            Log.e(ConversationViewModel.TAG, ex.stackTraceToString())
        }
    }

    interface OnMessagesFetched {
        fun showMessages(messages: ArrayList<Message>, isNew: Boolean)
    }

    companion object {
        const val TAG = "Messages"
    }
}