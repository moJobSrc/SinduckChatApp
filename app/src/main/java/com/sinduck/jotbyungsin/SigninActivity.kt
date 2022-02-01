package com.sinduck.jotbyungsin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sinduck.jotbyungsin.Util.XmppUtil
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.iqregister.AccountManager
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Localpart
import java.lang.Exception
import java.net.InetAddress
import javax.net.ssl.HostnameVerifier


class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        register.setOnClickListener {
            if (id.text.toString().isEmpty() || pw.text.toString().isEmpty())
                return@setOnClickListener

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val addr = InetAddress.getByName(XmppUtil.publicAddr)
                    val serviceName = JidCreate.domainBareFrom(XmppUtil.Domain)
                    val hostnameVerifier = HostnameVerifier { s, sslSession -> false }

                    val config = XMPPTCPConnectionConfiguration.builder()
                        .setPort(5222)
                        .setSendPresence(true)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setXmppDomain(serviceName)
                        .setHostnameVerifier(hostnameVerifier)
                        .setHostAddress(addr)
                        .build()

                    val connection: AbstractXMPPConnection = XMPPTCPConnection(config).connect()

                    AccountManager.getInstance(connection).apply {
                        sensitiveOperationOverInsecureConnection(true)
                        createAccount(Localpart.from(id.text.toString()), pw.text.toString())
                        connection.disconnect()
                    }
                    runOnUiThread {
                        Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(applicationContext, "FAILED", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}