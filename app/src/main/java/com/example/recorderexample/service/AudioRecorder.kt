package com.example.recorderexample.service

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import com.example.recorderexample.encodeFileToBase64

class AudioRecorder(
    private val context: Context,
) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""

    fun startRecording() {
        outputFile = "${context.filesDir}/recording.3gp" // TODO: 拡張子をFormat変えたら変える
        mediaRecorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // １番

            // Output
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // ２番 // TODO 確認
            setOutputFile(outputFile)

            // Input
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB) // TODO 確認
            setAudioEncodingBitRate(16) // TODO 指定方法確認、Formatによって使えない場合
//            setAudioChannels(1)
//            setAudioSamplingRate(16_000)

            // Prepare / Start
            prepare()
            start()
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    fun getOutputFile(): String {
        val base64 = encodeFileToBase64(outputFile)
        Log.d("base64", base64)

        return outputFile
    }
}