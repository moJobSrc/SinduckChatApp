package com.sinduck.jotbyungsin

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sinduck.jotbyungsin.Util.Databases
import com.sinduck.jotbyungsin.Util.MessageExtension
import com.sinduck.jotbyungsin.Util.XmppConnectionManager.mConnection
import com.sinduck.jotbyungsin.Util.XmppUtil
import kotlinx.android.synthetic.main.activity_chat_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smackx.offline.OfflineMessageManager
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest
import org.jxmpp.jid.impl.JidCreate
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatLayout : AppCompatActivity() {
    private var mAdapter: Adapter? = null
    private var mMessagesData = ArrayList<MessagesData>()
    private lateinit var currentChat: Chat
    private lateinit var chatManager: ChatManager
    private lateinit var sendTo: String
    private lateinit var database: Databases

    private val TAG: String = ChatLayout::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_layout)
        database = Databases(applicationContext, version = 1)
        sendTo = intent.getStringExtra("user")!!
        getHistory()
        partner.text = sendTo.split("@")[0]
        mAdapter = Adapter(mMessagesData)
        val manager = LinearLayoutManager(this)
        rv.layoutManager = manager
        rv.adapter = mAdapter
        rv.scrollToPosition(mMessagesData.size - 1)
        val deliveryManager = DeliveryReceiptManager.getInstanceFor(mConnection)
        deliveryManager.addReceiptReceivedListener { fromJid, toJid, receiptId, receipt ->
            Log.i("delivery", "for: $receiptId received")
        }

        chatManager = ChatManager.getInstanceFor(mConnection)
        currentChat = chatManager.chatWith(JidCreate.entityBareFrom(sendTo /** form name@192.168.0.105 **/))
        val mOfflineMessageManager = OfflineMessageManager(mConnection)
        mOfflineMessageManager.messages.forEach {
            if (it.from.asBareJid().toString() == sendTo) {
                mMessagesData.add(MessagesData(sendTo, "TIME", it.body, false))
                Log.d(TAG, it.body)
            }
        }
        saveHistory()
        mOfflineMessageManager.deleteMessages()
        setMsgListener()
        sendButton.setOnClickListener {
            val messageSend = inputText.text.toString()
            if (messageSend.isNotEmpty()) {
                sendMessage(messageSend)
                inputText.setText("")
            }
        }
        back.setOnClickListener {
            finish()
        }

    }

    private fun setMsgListener() {
//        currentChat\
//        ChatManagerListener {chat, _ ->
//            chat.addMessageListener { chat, message ->
//                chat.participant
//                Log.d("CHAT", chat.participant.toString())
//            }
//        }
        chatManager.addIncomingListener { from, message, chat ->
            //시간은 메시지에 포함할지 모르겠음 아니면 서버
            if (message != null) {
                Log.d(TAG, message.toString())
                Log.d(TAG, "barejid:${message.from.asBareJid()} || addrPartner:${chat.xmppAddressOfChatPartner}")
                //받은사람이 같은 서버 사용자인지 확인 && 받은 메시지가 같은사람인지 확인
                if (message.from.asBareJid() == chat.xmppAddressOfChatPartner && message.from.asBareJid().toString() == sendTo) {
                    //외부 통신 XMPP 메시지 에러 핸들링
                    val data = try {
                        //왜하는지 모름
                        ProviderManager.addExtensionProvider(
                            MessageExtension().elementName,
                            MessageExtension().namespace,
                            MessageExtension().Provider()
                        )
                        //시간 클라이언트 기준으로 추가->
                        //check for message with time extension
//                        val packetExtension: ExtensionElement = message.getExtension(MessageExtension().elementName, MessageExtension().namespace)
//                        Log.d("Packet", message.toXML("").toString())
//                        val packetTime = packetExtension.toXML("").toString()
//                            .replace("xmlns:stream='http://etherx.jabber.org/streams'", "")
//                            .replace("<","").replace(">","")
//                            .replace("time", "")
//                            .replace("xmlns='stamp:'", "").replace("/","")
//                            .replace("mTime='","").replace("'","").replace(" ","")
//                        val ext = MessageExtension().apply { setTimeMessage(packetTime) }
//                        Log.d("time:", packetTime)
////                        ext.setTimeMessage(packet.)
//                        Log.e("--->", " ---  LOG REPLY EXTENSION ---")
//                        Log.e("--->", ext.toXML("").toString() + "")
//                        Log.e(
//                            "--->",
//                            ext.getTimeMessage().toString() + ""
//                        ) //this is custom attribute
                        MessagesData(
                            sendTo.split("@")[0],
                            "TIME",
                            message.body.toString(), false
                        )
                    } catch (e: NullPointerException) {
                        XmppUtil.getMessageBodyWithoutXML(message)
                    } catch (e: ClassCastException) {
                        XmppUtil.getMessageBodyWithoutXML(message)
                    }

                    Log.e(TAG, "MSG::" + from + ": " + data.messages)
                    runOnUiThread {
                        mMessagesData.add(data)
                        saveHistory()
                        mAdapter?.notifyItemInserted(mMessagesData.size)
                        rv.scrollToPosition(mMessagesData.size - 1)
                    }

                }
            }
            Log.w("app", chat.toString())
        }
    }

    //메시지 전송
    @SuppressLint("SimpleDateFormat")
    private fun sendMessage(messageSend: String) {
        val chatManager = ChatManager.getInstanceFor(mConnection)

        //adding custom time extension
        val msgExt = MessageExtension()
        msgExt.setTimeMessage(SimpleDateFormat("a hh:mm").format(Date()))

        //Construct Message XML
        val message = Message().apply {
            type = Message.Type.chat
            addBody("MOOTIME","오후무상시")
//            from = mConnection.user
//            to = currentChat.xmppAddressOfChatPartner
            body = messageSend
//            addExtension(msgExt)
        }

        Log.e("message --->", message.toXML("").toString())
        try {
            DeliveryReceiptRequest.addTo(message)
            currentChat.send(message)

            val data = MessagesData("", msgExt.getTimeMessage()?: "", messageSend, true)
            mMessagesData.add(data)
            mAdapter?.notifyItemInserted(mMessagesData.size)
            rv.scrollToPosition(mMessagesData.size - 1)
            saveHistory()
        } catch (e: SmackException.NotConnectedException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun saveHistory() {
        GlobalScope.launch(Dispatchers.IO) {
            val gson = Gson()
            val history = gson.toJson(mMessagesData)
            database.insertAndUpdate(sendTo, history)
            Log.d("SAVE", history)
        }
    }

    private fun getHistory() {
        mMessagesData = try {
            val gson = Gson()
            val history = database.getHistory(sendTo)?: ""
            val type: Type = object : TypeToken<ArrayList<MessagesData?>?>() {}.type
            gson.fromJson(history, type)
        } catch (e: java.lang.NullPointerException) {
            ArrayList<MessagesData>()
        }
    }

    override fun onResume() {
        super.onResume()
        val presence = Presence(Presence.Type.available)
        mConnection.sendStanza(presence)
    }
}