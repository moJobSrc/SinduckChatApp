package com.sinduck.jotbyungsin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.android.synthetic.main.receivemsg_layout.view.*
import kotlinx.android.synthetic.main.receivemsg_layout.view.messageBody
import kotlinx.android.synthetic.main.sendmsg_layout.view.messageBody


class Adapter(val mMessagesData: ArrayList<MessagesData>) : RecyclerView.Adapter<ViewHolder>() {
    final val viewTypeRight:Int = 1;
    final val viewTypeLeft:Int = 0;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == viewTypeRight) {
            sendHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.sendmsg_layout, parent, false)
            )
        } else {
            receiveHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.receivemsg_layout, parent, false)
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is receiveHolder) {
            val data: MessagesData = mMessagesData[position]
            holder.heading.text = data.heading
            holder.messages.text = data.messages
            holder.time.text = data.time
        } else if (holder is sendHolder) {
            val data: MessagesData = mMessagesData[position]
            holder.messages.text = data.messages
        }

    }

    override fun getItemCount() = mMessagesData.size

    override fun getItemViewType(position: Int): Int {
        return if (mMessagesData[position].ownMessage) {
            viewTypeRight
        } else {
            viewTypeLeft
        }
    }

    inner class receiveHolder(itemView: View) : ViewHolder(itemView) {
        var heading: TextView = itemView.heading
        var messages: TextView = itemView.findViewById(R.id.messageBody)
        var time: TextView = itemView.item_date
    }

    inner class sendHolder(itemView: View) : ViewHolder(itemView) {
        var messages: TextView = itemView.findViewById(R.id.messageBody)
    }
}