package com.example.recorderexample.service

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log

class AudioHelper(context: Context) {
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun routeAudioToBluetooth() {
        val devices = audioManager.availableCommunicationDevices
        Log.d("Audio Helper route", devices.toString())

        for (d in devices) {
            if (d.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                Log.d("Audio Helper route", d.toString())
                audioManager.setCommunicationDevice(d)
                audioManager.isBluetoothScoOn = true
            }
        }
    }

    fun stopBluetoothSco() {
        audioManager.isBluetoothScoOn = false
        audioManager.clearCommunicationDevice()
    }
}
