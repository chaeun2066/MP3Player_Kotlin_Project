package com.example.mp3player

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object{
        val REQ_READ = 99
        val DB_NAME = "musicDB"
        val VERSION = 1
    }

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MusicRecyclerAdapter
    private var musicList: MutableList<Music>? = mutableListOf<Music>()
    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(isPermitted()){
            startProcess()
        }else{
            ActivityCompat.requestPermissions(this, permissions, REQ_READ)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQ_READ && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startProcess()
        }else {
            Toast.makeText(this,"권한 요청을 승인하셔야만 MP3 이용이 가능합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun startProcess() {
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        musicList = dbHelper.selectMusicAll()

        if(musicList == null){
            val playMusicList = getMusicList()
            if(playMusicList != null){
                for(i in 0 .. playMusicList.size - 1){
                    val music = playMusicList.get(i)
                    dbHelper.insertMusic(music)
                }
                musicList = playMusicList
            }else{
                Log.d("com.example.mp3player", "MainActivity.startProcess() 외장메모리에 음원파일 없음")
            }
        }

        adapter = MusicRecyclerAdapter(this, musicList)
        binding.recylerview.layoutManager = LinearLayoutManager(this)
        binding.recylerview.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(binding.recylerview.context, LinearLayoutManager(this).orientation)
        binding.recylerview.addItemDecoration(dividerItemDecoration)
        binding.recylerview.adapter = adapter
    }

    fun getMusicList(): MutableList<Music>? {
        var imsiMusicList: MutableList<Music>? = mutableListOf<Music>()
        val musicURL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        val cursor = contentResolver.query(musicURL, projection, null, null, null)
        if(cursor?.count!! > 0) {
            while (cursor!!.moveToNext()){
                val id = cursor.getString(0)
                val title = cursor.getString(1).replace("'", "")
                val artist = cursor.getString(2).replace("'", "")
                val albumId = cursor.getString(3)
                val duration = cursor.getInt(4)

                val music = Music(id, title, artist, albumId, duration, 0)

                imsiMusicList?.add(music)
            }
        }else {
            imsiMusicList = null
        }
        return imsiMusicList
    }

    fun isPermitted(): Boolean {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchMenu = menu?.findItem(R.id.menu_search)
        val searchview = searchMenu?.actionView as SearchView

        searchview.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(query: String?): Boolean {
                val dbHelper = DBHelper(applicationContext, MainActivity.DB_NAME, MainActivity.VERSION)
                if(query.isNullOrBlank()){
                    musicList?.clear()
                    dbHelper.selectMusicAll()?.let{musicList?.addAll(it)}
                    adapter.notifyDataSetChanged()
                }else{
                    musicList?.clear()
                    dbHelper.searchMusic(query)?.let{musicList?.addAll(it)}
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dbHelper = DBHelper(applicationContext, MainActivity.DB_NAME, MainActivity.VERSION)
        when(item.itemId){
            R.id.menu_like -> {
                musicList?.clear()
                dbHelper.selectMusicLike()?.let{musicList?.addAll(it)}
                adapter.notifyDataSetChanged()
            }
            R.id.menu_main -> {
                musicList?.clear()
                dbHelper.selectMusicAll()?.let{musicList?.addAll(it)}
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}