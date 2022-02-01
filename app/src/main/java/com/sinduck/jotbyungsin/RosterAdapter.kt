package com.sinduck.jotbyungsin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sinduck.jotbyungsin.Util.XmppConnectionManager
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jxmpp.jid.Jid


class RosterAdapter(
    private val data: List<RosterEntry>,
    private val map: Map<Jid, List<Message>>
) :
    RecyclerView.Adapter<RosterAdapter.MyViewHolder>() {
    val roster = Roster.getInstanceFor(XmppConnectionManager.mConnection)
    private var listener: RoasterClickListener? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.tvName)
        var tvCount: TextView = view.findViewById(R.id.tvCount)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_roaster, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val roaster = data[position]
        var offlineMessages: List<Message> = arrayListOf()
        holder.name.text = roaster.name
        holder.itemView.setOnClickListener {
            listener?.onClick(roaster)
        }
        for (key in map.keys) {
            if (key.contains(roaster.jid)) {
                offlineMessages = map[key]!!
            }

        }

        // 원래 tv count는 오프라인 메시지가 있는지없는지 확인이였음 -> 온 오프라인 변경
        if (!offlineMessages.isNullOrEmpty()) {
            holder.tvCount.visibility = View.VISIBLE
//            holder.tvCount.text = offlineMessages.size.toString()
        } else {
            holder.tvCount.visibility = View.VISIBLE
        }
        /* var isOnline = false
         if (presence != null) {
             for (p in presence!!) {
                 if(roaster.user.contains(p.from)){
                     isOnline=true
                     break
                 }
             }
         }*/
        if (roster.getPresence(roaster.jid).isAvailable)
            holder.tvCount.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_green_light
                )
            )
        else {
            holder.tvCount.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.darker_gray
                )
            )
        }

    }

    fun setRoasterListener(listener: RoasterClickListener) {
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return data.size
    }


    interface RoasterClickListener {
        fun onClick(
            entry: RosterEntry
        )
    }
}