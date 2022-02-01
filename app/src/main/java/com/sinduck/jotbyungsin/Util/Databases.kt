package com.sinduck.jotbyungsin.Util

import android.content.Context
import android.database.CursorIndexOutOfBoundsException
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class Databases(
    context: Context?,
    version: Int
) : SQLiteOpenHelper(context, "chatlog.db", null, version, null) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Chatting(id TEXT UNIQUE, history Text UNIQUE)")
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS Chatting")
        onCreate(db)
    }

    fun insertAndUpdate(id: String, history: String) {
        val db = writableDatabase
        db.execSQL("INSERT OR REPLACE INTO Chatting VALUES('$id','$history')")
//        db.execSQL("INSERT INTO Chatting VALUES('id = $id, history = $history') ON DUPLICATE KEY UPDATE id=$id,history=$history WHERE id = '$id'")
        db.close()
    }

    fun getHistory(id: String): String? {
        val db = readableDatabase
        val result = db.rawQuery("SELECT * FROM Chatting WHERE id='$id'", null)
        result.moveToFirst()
        Log.d("HISTROY", result.getString(1))
        return result.getString(1)
    }

    fun reset() {
        val db = writableDatabase
        db.execSQL("delete from Chatting")
    }

}