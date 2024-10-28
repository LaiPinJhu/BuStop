package com.example.bustop

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import retrofit2.Call
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask

class MyService : Service() {
    private var cameraManager: CameraManager? = null
    private var vibrator: Vibrator? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var isFlashAndVibrateEnabled = false
    private var sets: String? = null
    private var PlateNumb: String? = null
    private var stopName: String? = null
    private var D: String? = null
    private var nText: String? = null
    private var timer= Timer()


    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
//        registerReceiver()

    }

    override fun onStartCommand(intent: Intent? , flags: Int, startId: Int): Int {
//        sets = intent?.getStringExtra("setsText")
//        stopName = intent?.getStringExtra("stopNameTextView")
//        D = intent?.getStringExtra("D")
//        nText = intent?.getStringExtra("n")
        nText =
            getSharedPreferences("Data", 0).getString("routeNumber", "尚無資料").toString()
        D =
            getSharedPreferences("Data", 0).getString("Direct", "尚無資料").toString()
        stopName = getSharedPreferences("Data", 0).getString("stop", "尚無資料").toString()
        sets = getSharedPreferences("Data", 0).getString("sets", "尚無資料").toString()
        checkAndTriggerFlashLightAndVibrate()
        startChecking()
        return  START_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
//        unregisterReceiver()
//        stopChecking()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

//    private fun registerReceiver() {
//        val filter = IntentFilter("jjj")
//        broadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                sets = intent.getStringExtra("sets")
//                nextBusTime = intent.getStringExtra("nextBusTime")
//                checkAndTriggerFlashLightAndVibrate()
//            }
//        }
//        registerReceiver(broadcastReceiver, filter)
//    }

//    private fun unregisterReceiver() {
//        unregisterReceiver(broadcastReceiver)
//    }

    private fun startChecking() {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                getData()
            }
        }, 0, 10000)
    }

//    private fun stopChecking() {
//        timer!!.cancel()
//    }

    private fun checkAndTriggerFlashLightAndVibrate() {
//        if (sets != null && nextBusTime != null && sets == nextBusTime) {
//            if (!isFlashAndVibrateEnabled) {
//                flashLightAndVibrate()
//                isFlashAndVibrateEnabled = true
//            }
//        } else {
//            isFlashAndVibrateEnabled = false
        }
private fun getData() {
    UVApi.retrofitService.getUV(format = "JSON")
        .enqueue(object : retrofit2.Callback<List<BusData>> {
            override fun onFailure(call: Call<List<BusData>>, t: Throwable) {
                Log.e("fail", t.toString())
            }

            override fun onResponse(
                call: Call<List<BusData>>,
                response: Response<List<BusData>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.forEach {
                        Log.d("api", it.toString())
                        if (it.Direction == D &&
                            it.RouteName.Zh_tw == nText?.substring(0, 2) &&
                            it.StopName.Zh_tw == stopName

                        ) {

                            Log.d("wwwww",PlateNumb+" "+it.PlateNumb)
                            if (PlateNumb != it.PlateNumb){
                                if (!(it.Estimates.isNullOrEmpty())) {
                                    Log.d("ddd", it.Estimates[0].EstimateTime)
                                    var minutesRemaining =
                                        it.Estimates[0].EstimateTime.toLong() / 60
                                    if (sets?.toInt()!! >= minutesRemaining.toInt()) {
                                        Log.d("qqqq", minutesRemaining.toString())
                                        flashLightAndVibrate()
                                        PlateNumb = it.PlateNumb
                                    }

                                } else {
                                    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                    val dateTime = OffsetDateTime.parse(it.NextBusTime, formatter)
                                    val localDateTime = dateTime.toLocalDateTime()
                                    val currentTime = LocalDateTime.now()
                                    Log.d("dddd", localDateTime.toString())
                                    if (localDateTime.isAfter(currentTime)) {
                                        val minutesRemaining =
                                            Duration.between(currentTime, localDateTime).toMinutes()
                                        if (sets?.toInt()!! >= minutesRemaining.toInt()) {
                                            flashLightAndVibrate()
                                            PlateNumb = it.PlateNumb
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun flashLightAndVibrate() {
        try {
            val cameraListId = cameraManager!!.cameraIdList[0]
            cameraManager!!.setTorchMode(cameraListId, true)
            for (i in 1..5) {
                Thread.sleep(500)
                cameraManager!!.setTorchMode(cameraListId, false)
                Thread.sleep(500)
                cameraManager!!.setTorchMode(cameraListId, true)
            }
            cameraManager!!.setTorchMode(cameraListId, false)
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            vibrator!!.vibrate(pattern, -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}