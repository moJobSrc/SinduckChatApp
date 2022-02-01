package com.sinduck.jotbyungsin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sinduck.jotbyungsin.Util.XmppConnectionManager
import com.sinduck.jotbyungsin.Util.XmppConnectionManager.mConnection
import com.sinduck.jotbyungsin.Util.XmppUtil
import kotlinx.android.synthetic.main.activity_chat_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.roster.RosterListener
import org.jivesoftware.smack.roster.packet.RosterPacket
import org.jivesoftware.smackx.offline.OfflineMessageManager
import org.jxmpp.jid.Jid
import org.jivesoftware.smack.XMPPException

import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.SmackException.*
import org.jivesoftware.smack.XMPPException.XMPPErrorException
import org.jxmpp.jid.impl.JidCreate
import java.lang.Exception


class ChatList : AppCompatActivity(), RosterAdapter.RoasterClickListener {
    private var rosterLists: ArrayList<RosterEntry> = ArrayList()
    private lateinit var adapter: RosterAdapter
    private val roster = Roster.getInstanceFor(mConnection)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        roster.subscriptionMode = Roster.SubscriptionMode.accept_all
//        mConnection.addPacketInterceptor(StanzaListener { packet ->
//            if (packet is Presence) {
//                runOnUiThread {
//                    Toast.makeText(
//                        applicationContext,
//                        "PRESENCES" + packet.from,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }, object : StanzaFilter {
//            override fun accept(stanza: Stanza?): Boolean {
//                return stanza is Presence
//            }
//        })
        logout.setOnClickListener {
            val preferences = getSharedPreferences("userAbout", Context.MODE_PRIVATE)
            val editor = preferences.edit();
            editor.remove("id");
            editor.remove("pw");
            editor.apply()
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
            mConnection.disconnect()
        }
        addFriend.setOnClickListener {
            if (mConnection != null && mConnection.isConnected) {
                try {
                    roster.createEntry(
                        JidCreate.bareFrom(friendName.text.toString()+"@${XmppUtil.Domain}"),
                        friendName.text.toString(),
                        null
                    )
                } catch (e: NotLoggedInException) {
                    e.printStackTrace()
                } catch (e: NoResponseException) {
                    e.printStackTrace()
                } catch (e: NotConnectedException) {
                    e.printStackTrace()
                } catch (e: XMPPErrorException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        roster.addRosterListener(object: RosterListener {
            override fun entriesAdded(addresses: MutableCollection<Jid>?) {
                getBuddies()
            }

            override fun entriesUpdated(addresses: MutableCollection<Jid>?) {
                getBuddies()
            }

            override fun entriesDeleted(addresses: MutableCollection<Jid>?) {
                getBuddies()
            }

            override fun presenceChanged(presence: Presence?) {
                getBuddies()
            }

        })

        val offlineMessageManager = OfflineMessageManager(mConnection)
        val map = offlineMessageManager.messages.groupBy { it.from }
        val presence = Presence(Presence.Type.available)
        mConnection.sendStanza(presence)
        adapter = RosterAdapter(rosterLists, map)
        adapter.setRoasterListener(this)
        chatListRv.layoutManager = LinearLayoutManager(this)
        chatListRv.adapter = adapter
    }

    fun getBuddies() {
        GlobalScope.launch(Dispatchers.IO) {
            val entries = roster.entries
            Log.e("Size of Roster :", "" + entries?.size)
            if (entries != null) {
                rosterLists.clear()
                for (entry in entries) {
                    rosterLists.add(entry)
                    if (entry.type == RosterPacket.ItemType.from) {
                        roster.createEntry(
                            entry.jid,
                            entry.jid.asUnescapedString(),
                            null
                        )
                    }
                }
            }
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
//        val presence = roster.getAllPresences(Config.loginName + "@" + Config.openfire_host_server_SERVICE)
//
//        adapter.setPresence(presence)
        }
    }

    override fun onClick(
        entry: RosterEntry
    ) {
        val intent = Intent(this, ChatLayout::class.java)
        intent.putExtra("user", entry.jid.asUnescapedString())
        Log.d("intent():",entry.jid.asUnescapedString())
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val presence = Presence(Presence.Type.unavailable)
        mConnection.sendStanza(presence)
        mConnection.disconnect()
    }

//    override fun onPause() {
//        super.onPause()
//        val presence = Presence(Presence.Type.unavailable)
//        mConnection.sendStanza(presence)
//    }

    override fun onResume() {
        super.onResume()
        val presence = Presence(Presence.Type.available)
        mConnection.sendStanza(presence)
        getBuddies()
    }
}