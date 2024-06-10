package com.example.recorderexample.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AudioRecorder2(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val buffer = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var outputFile = "${context.filesDir}/recording.3gp"
    private var isRecording = false

    @SuppressLint("MissingPermission")
    fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            buffer,
        )

        val data = ByteArray(buffer)
        val fileOutputStream = FileOutputStream(outputFile)
        audioRecord?.startRecording()

        isRecording = true

        Thread {
            while (isRecording) {
                val read = audioRecord?.read(data, 0, buffer) ?: 0
                if (read > 0) {
                    fileOutputStream.write(data, 0, read)
                }
            }
            fileOutputStream.close()
            audioRecord?.stop()
            audioRecord?.release()
        }.start()
    }

    fun stopRecording() {
        isRecording = false
    }

    fun getOutputFile(): String {
        val base64 = encodeAudioFileToBase64()
        Log.d("base64", base64)

        return outputFile
    }

    private fun encodeAudioFileToBase64(): String {
        val audioFile = File(outputFile)
        Log.d("audio file", audioFile.toString())
        val fileInputStream = FileInputStream(audioFile)
        val bytesArray = ByteArray(audioFile.length().toInt())
        Log.d("bytes array", bytesArray.toString())
        fileInputStream.read(bytesArray)
        fileInputStream.close()
        return Base64.encodeToString(bytesArray, Base64.DEFAULT)
    }
}

class AudioPlayer(context: Context) {
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 16000  // 再生のサンプルレート
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO  // モノラル出力
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT  // 16ビット PCM
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var outputFile = "${context.filesDir}/recording.3gp"

    fun startPlayer() {
        val file = File(outputFile)
        val bytesArray = ByteArray(file.length().toInt())
        Log.d("AudioPlayer", "$bytesArray")
        audioTrack?.write(bytesArray, 0, bytesArray.size)
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM)
        audioTrack?.play()
    }

    fun stopPlayer() {
        audioTrack?.stop()
        audioTrack?.release()
    }
}