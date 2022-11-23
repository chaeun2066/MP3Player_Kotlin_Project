package com.example.mp3player

import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.SeekBar
import com.example.mp3player.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import kotlin.math.max

class PlaymusicActivity : AppCompatActivity() {
    companion object{
        val ALBUM_SIZE = 350
    }

    private lateinit var binding: ActivityPlaymusicBinding
    private var playList:MutableList<Parcelable>? = null
    private var position: Int = 0
    private var music: Music? = null
    private var mediaPlayer: MediaPlayer? = null
    private var messengerJob: Job? =null
    private var loopFlag: Boolean = false

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvPlayTitle.isSelected = true
        binding.tvPlayArtist.isSelected = true

        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position",0)
        music = playList?.get(position) as Music

        binding.tvPlayTitle.text = music?.title
        binding.tvPlayArtist.text = music?.artist
        binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        binding.tvPlayDuration.text = "00:00"

        val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if(bitmap != null){
            binding.ivPlayAlbumart.setImageBitmap(bitmap)
        }else {
            binding.ivPlayAlbumart.setImageResource(R.drawable.ic_music_24)
        }

        mediaPlayer = MediaPlayer.create(this,music?.getMusicUri())

        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d("com.example.mp3player", "움직일 때 호출")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d("com.example.mp3player", "움직임이 끝났을 때 호출")
            }
        })

        binding.ivListBack.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            finish()
        }

        binding.ivStop.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.progress = 0
            binding.tvPlayDuration.text = "00:00"
            binding.ivPlay.setImageResource(R.drawable.ic_play_24)
        }

        binding.ivPlay.setOnClickListener {
            if(mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
                binding.ivPlay.setImageResource(R.drawable.ic_play_24)
            }else {
                if(loopFlag == false) {
                    mediaPlayer?.start()
                    binding.ivPlay.setImageResource(R.drawable.ic_pause_24)
                    musicStart()
                }else {
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.start()
                    binding.ivPlay.setImageResource(R.drawable.ic_pause_24)
                    musicStart()
                }
            }
        }

        binding.ivPrevious.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.progress = 0
            binding.tvPlayDuration.text = "00:00"
           if (position >= 0) {
                if (position == 0) {
                    position = playList!!.size - 1
                } else {
                    --position
                }
                music = playList?.get(position) as Music

                binding.tvPlayTitle.text = music?.title
                binding.tvPlayArtist.text = music?.artist
                binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
                binding.tvPlayDuration.text = "00:00"

                val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
                if (bitmap != null) {
                    binding.ivPlayAlbumart.setImageBitmap(bitmap)
                } else {
                    binding.ivPlayAlbumart.setImageResource(R.drawable.ic_music_24)
                }
                mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            }
        }

        binding.ivNext.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.progress = 0
            binding.tvPlayDuration.text = "00:00"
                  if(position < playList!!.size) {
                    if(position == playList!!.size - 1){
                        position = 0
                    }else {
                        ++position
                    }

                    music = playList?.get(position) as Music

                    binding.tvPlayTitle.text = music?.title
                    binding.tvPlayArtist.text = music?.artist
                    binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
                    binding.tvPlayDuration.text = "00:00"

                    val bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
                    if (bitmap != null) {
                        binding.ivPlayAlbumart.setImageBitmap(bitmap)
                    } else {
                        binding.ivPlayAlbumart.setImageResource(R.drawable.ic_music_24)
                    }
                    mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
                }
        }
    }

    fun musicStart(){
        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
        messengerJob = backgroundScope.launch {
            while(mediaPlayer?.isPlaying == true){
                runOnUiThread{
                    var currentPosition = mediaPlayer?.currentPosition!!
                    binding.seekBar.progress = currentPosition
                    val currentDuration = SimpleDateFormat("mm:ss").format(mediaPlayer!!.currentPosition)
                    binding.tvPlayDuration.text = currentDuration
                }
                try{
                    delay(100)
                }catch(e:java.lang.Exception){
                    Log.d("com.example.mp3player","Thread Error")
                }
            }
            runOnUiThread{
                if(mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)){
                    binding.seekBar.progress = 0
                    binding.tvPlayDuration.text = "00:00"
                }
                binding.ivPlay.setImageResource(R.drawable.ic_play_24)
            }
        }
    }
}