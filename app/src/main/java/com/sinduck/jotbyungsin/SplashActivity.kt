package com.sinduck.jotbyungsin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.sinduck.jotbyungsin.Util.Databases
import com.sinduck.jotbyungsin.Util.XmppConnectionManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
/**   Splash Acitivity를 Full Screen 으로 만들어 주는 것     **/
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            intent()
        }

        handler.postDelayed(runnable, 3000)
        animationView.setOnClickListener{
            handler.removeCallbacks(runnable)
            intent()
        }
    }

    fun intent() {
        val preferences = getSharedPreferences("userAbout", Context.MODE_PRIVATE)
        val id = preferences.getString("id","")!!
        val pw = preferences.getString("pw","")!!
        if (id.isNotEmpty() || pw.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                val result = XmppConnectionManager.setConnection(
                    id,
                    pw
                )
                runOnUiThread {
                    if (result) {
                        Log.e("", "SUCCESS AUTH")
                        startActivity(Intent(applicationContext, ChatList::class.java))
                        finish()
                    } else {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}