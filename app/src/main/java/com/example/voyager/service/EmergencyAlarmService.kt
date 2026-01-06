package com.example.voyager.service


import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.*

class EmergencyAlarmService(private val context: Context) {

    private var alarmJob: Job? = null
    private var toneGenerator: ToneGenerator? = null
    private val vibrator =
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun startAlarm() {
        stopAlarm() // stop any existing alarm

        toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        alarmJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                toneGenerator?.startTone(
                    ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,
                    500
                )
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
                delay(2000)
            }
        }
    }

    fun stopAlarm() {
        alarmJob?.cancel()
        alarmJob = null
        toneGenerator?.release()
        toneGenerator = null
        vibrator.cancel()
    }
}
