package com.example.recorderexample.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Process
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.log10
import kotlin.math.sqrt

class RecorderManager constructor(
    private val context: Context,
) {
    // Publish
    private val _endRecording = Channel<String>()
    val endRecording: Flow<String> = flow {
        for (e in _endRecording) emit(e)
    }.flowOn(Dispatchers.Default)

    // Private
    private var audioRecord: AudioRecord? = null
    private val sampleLate = 16_000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val buffer = AudioRecord.getMinBufferSize(sampleLate, channelConfig, audioFormat)

    private var audioTrack: AudioTrack? = null

    private var outputFile = "${context.filesDir}/recording.3gp"
    private var isRecording = false

    private enum class VoiceStatus {
        BEFORE, RECORDING, SILENCE,
    }

    private var voiceStatus = VoiceStatus.BEFORE
    private var timerJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Default)
    private val timerScope = CoroutineScope(Dispatchers.Default)

    // Public
    @SuppressLint("MissingPermission")
    fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleLate,
            channelConfig,
            audioFormat,
            buffer,
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            // TODO: Handling
            return
        }

        val data = ByteArray(buffer)
        val fileOutputStream = FileOutputStream(outputFile)
        audioRecord?.startRecording()

        isRecording = true

        Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            try {
                while (isRecording) {
                    val read = audioRecord?.read(data, 0, buffer) ?: 0
                    if (read > 0) {
                        fileOutputStream.write(data, 0, read)
                        calcVolume(data, read)
//                    } else if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
//                        // エラー処理
//                        Log.d("RecorderManager", "else if")
//                        break
//                    } else {
//                        // TODO: 確認
//                        Log.d("RecorderManager", "else")
////                        throw IllegalAccessError()
//                        break
                    }
                }
            } catch (e: Exception) {
                // TODO: Error Handling
                Log.d("RecorderManager", "start record ${e.message}")
//                e.printStackTrace()
            } finally {
                Log.d("RecorderManager", "close")
                fileOutputStream.close()
                stopRecording()
            }
        }.start()
    }

    fun stopRecording() {
        stop()

        val mediaPlayer = MediaPlayer()
        try {
//            mediaPlayer.setDataSource(outputFile)
//            mediaPlayer.prepare()
//            mediaPlayer.start()
            val file = File(outputFile)
            val audioData = file.readBytes()

            audioTrack = AudioTrack.Builder()
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleLate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(buffer)
                .build()

            audioTrack?.play()
            CoroutineScope(Dispatchers.IO).launch {
                audioTrack?.write(audioData, 0, audioData.size)
            }
        } catch (e: IOException) {
            Log.d("RecorderManager", "media player ${e.localizedMessage}" ?: "")
        }
    }

    private fun stop() {
        Log.d("RecorderManager", "stop")

//        scope.launch {
//            _endRecording.send(getOutputAudio())
//        }

        try {
            audioRecord?.let {
                Log.d("RecorderManager", "recording state: ${it.recordingState}")
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: IllegalStateException) {
            // TODO: Handling
//            e.printStackTrace()
            Log.d("RecorderManager", "stop ${e.message}")

        } finally {
            audioRecord = null
            stopTimer()
            isRecording = false
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun getOutputAudio(): String {
        return encodeAudioFileToBase64()
    }

    private fun encodeAudioFileToBase64(): String {
        val audioFile = File(outputFile)
        val fileInputStream = FileInputStream(audioFile)
        val bytesArray = ByteArray(audioFile.length().toInt())
        fileInputStream.read(bytesArray)
        fileInputStream.close()
        return Base64.encodeToString(bytesArray, Base64.DEFAULT)
    }

    private fun calcVolume(buffer: ByteArray, read: Int) {
        var sum = 0.0
        for (i in 0 until read) {
            sum += buffer[i] * buffer[i]
        }
        val rms = sqrt(sum / read)
        val db = 20 * log10(rms)

        // TODO: 確認
        Log.d("Volume", "$db")
        val threshold = 30

        if (voiceStatus == VoiceStatus.BEFORE && db > threshold) {
            // 録音開始後、話し出す前
            voiceStatus = VoiceStatus.RECORDING
        } else if (voiceStatus == VoiceStatus.RECORDING && db > threshold) {
            // 録音中
        } else if (voiceStatus == VoiceStatus.RECORDING && threshold >= db) {
            // 録音開始後、話を止めた
            voiceStatus = VoiceStatus.SILENCE

            timerJob = timerScope.launch {
                Log.d("RecorderManager", "timeup")
                delay(1000L)
                stopRecording()
            }
        } else if (voiceStatus == VoiceStatus.SILENCE && db >= threshold) {
            // 再度話しだした
            voiceStatus = VoiceStatus.RECORDING
            stopTimer()
        }
    }
}