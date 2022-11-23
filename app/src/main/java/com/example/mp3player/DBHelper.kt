package com.example.mp3player

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, dbName: String, version: Int):SQLiteOpenHelper(context, dbName, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = """
            create table musicTBL(id text primary key, title text, artist text, albumId text, duration integer, likes integer)
        """.trimIndent()
        db?.execSQL(query)
        Log.d("com.example.mp3player", "DBHelper.onCreate()")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query = """
            drop table musicTBL
        """.trimIndent()
        db?.execSQL(query)
        this.onCreate(db)
    }

    fun selectMusicAll():MutableList<Music>?{
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """ select * from musicTBL """.trimIndent()
        val db = this.readableDatabase

        try {
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            }else {
                musicList = null
            }
        }catch (e:java.lang.Exception){
            Log.d("chap17_mp3db", "DBHelper selectMusicAll() ")
            musicList = null
        }finally {
            cursor?.close()
        }
        return musicList
    }

    fun insertMusic(music: Music):Boolean {
        var flag:Boolean
        val query = """insert into musicTBL(id, title, artist, albumId, duration, likes)
            values ('${music.id}', '${music.title}', '${music.artist}', '${music.albumId}', ${music.duration}, ${music.likes})""".trimIndent()
        val db = this.writableDatabase
        Log.d("com.example.mp3player", "DBHelper insertMusic() success1 ${music.title}")

        try {
            db.execSQL(query)
            flag = true
            Log.d("com.example.mp3player", "DBHelper insertMusic() success2 ${music.title}")
        }catch (e : java.lang.Exception){
            Log.d("com.example.mp3player", "DBHelper insertMusic() failed ${e.toString()}")
            flag = false
        }
        return flag
    }

    fun updateLike(music:Music): Boolean{
        var flag:Boolean
        val query = """
            update musicTBL set likes = ${music.likes} where id = '${music.id}'
        """.trimMargin()

        val db = this.writableDatabase
        Log.d("com.example.mp3player", "DBHelper updateLike() success1 ${music.likes}")

        try{
            db.execSQL(query)
            flag = true
            Log.d("com.example.mp3player", "DBHelper updateLike() success2 ${music.likes}")
        }catch(e:java.lang.Exception){
            Log.d("com.example.mp3player", "DBHelper updateLike() failed ${music.likes}")
            flag = false
        }
        return flag
    }

    fun searchMusic(query: String?):MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """ select * from musicTBL where title like '${query}%' or artist like '${query}%' """.trimIndent()
        val db = this.readableDatabase

        try{
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            }else {
                musicList = null
            }
        }catch (e:java.lang.Exception){
            Log.d("chap17_mp3db", "DBHelper selectMusicAll() ")
            musicList = null
        }finally {
            cursor?.close()
        }
        return musicList
    }

    fun selectMusicLike(): MutableList<Music>?  {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """ select * from musicTBL where likes = 1""".trimIndent()
        val db = this.readableDatabase
        try {
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            }else {
                musicList = null
            }
        }catch (e:java.lang.Exception){
            Log.d("chap17_mp3db", "DBHelper selectMusicLike() ")
            musicList = null
        }finally {
            cursor?.close()
        }
        return musicList
    }
}