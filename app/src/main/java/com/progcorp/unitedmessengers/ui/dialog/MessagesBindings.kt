@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.dialog

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Message

@BindingAdapter("bind_dialog_messages_list")
fun bindMessagesList(listView: RecyclerView, items: List<Message>?) {
    items?.let {
        (listView.adapter as MessagesListAdapter).submitList(items)
    }
}