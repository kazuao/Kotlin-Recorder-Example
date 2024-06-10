package com.example.recorderexample

import android.Manifest
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.recorderexample.service.AudioHelper
import com.example.recorderexample.service.AudioPlayer
import com.example.recorderexample.service.RecorderManager
import com.example.recorderexample.ui.theme.RecorderExampleTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAudioManager: AudioHelper
    private lateinit var audioPlayer: AudioPlayer

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("requestPermissionLauncher", "granted")
        } else {
            Log.d("requestPermissionLauncher", "denied")
            requestPermission()
        }
    }

    private val requestPermissionBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("requestPermissionLauncher Bluetooth", "granted")
        } else {
            Log.d("requestPermissionLauncher Bluetooth", "denied")
            requestPermissionBluetooth()
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        requestPermissionBluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }

    private fun requestPermissionBluetooth() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        requestPermissionBluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }

    private lateinit var recorderManager: RecorderManager
//    private lateinit var recorderManager2: RecorderManager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission()

//        recorderManager = RecorderManager(this)
//        recorderManager2 = RecorderManager2()
        audioPlayer = AudioPlayer(this)

        bluetoothAudioManager = AudioHelper(this)
        bluetoothAudioManager.routeAudioToBluetooth()


        setContent {
            RecorderExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

//                    LaunchedEffect(Unit) {
//                        recorderManager.endRecording.collect { base64 ->
//                            if (base64.isEmpty()) {
//                                return@collect
//                            }
////                            Log.d("MainActivity", "base64: $base64")
//                        }
//                    }


                    Column {
//                        StartButton(audioRecorder = recorderManager)
//                        StopButton(audioRecorder = recorderManager, audioPlayer = audioPlayer)

//                        Button(
//                            onClick = {
//                                if (recorderManager2.isRecording) {
//                                    recorderManager2.stopRecording()
//                                    recorderManager2.playRecording()
//                                } else {
//                                    recorderManager2.startRecording(getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!)
//                                }
//                            }
//                        ) {
//                            Text(text = if (recorderManager2.isRecording) "stop" else "start")
//                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        bluetoothAudioManager.stopBluetoothSco()
    }
}

@Composable
fun StartButton(audioRecorder: RecorderManager) {
    Button(
        onClick = {
            audioRecorder.startRecording()
        }
    ) {
        Text(text = "録音Start")
    }
}

@Composable
fun StopButton(audioRecorder: RecorderManager, audioPlayer: AudioPlayer) {
    Button(
        onClick = {
            audioRecorder.stopRecording()
//            val a = audioRecorder.getOutputFile()
//            Log.d("hoge", a)

//            audioPlayer.startPlayer()

//            MediaPlayer().apply {
//                setDataSource(audioRecorder.getOutputFile())
//                prepare()
//                start()
//                Log.d("MediaPlayer", "start")
//            }
        }
    ) {
        Text(text = "録音End")
    }
}

fun encodeFileToBase64(filePath: String): String {
    val fileContents = File(filePath).readBytes()
    return Base64.encodeToString(fileContents, Base64.DEFAULT)
}