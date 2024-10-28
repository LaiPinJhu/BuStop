package com.example.bustop

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bustop.databinding.ActivityBusReminderBinding
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


class BusReminder : AppCompatActivity() {

    private lateinit var binding: ActivityBusReminderBinding
    private lateinit var showNotificationBtn: Button
    private lateinit var clickButton: Button
    private val CHANNEL_ID: String = "channel01"
    private lateinit var stopNameTextView: TextView
    private lateinit var nextBusTimeTextView: TextView
    private lateinit var BBus: TextView
    private lateinit var n: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MyPrefs"
    private val KEY_USERNAME = "username"
    private var reminderTime: Int = 0
    private var routeNumber: String = "尚無資料"
    private var direction: String = "尚無資料"
    private var stop: String = "尚無資料"
    private var time: String = "尚無資料"
    private var D: String = "尚無資料"
    private var timer= Timer()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)


        D = intent.getStringExtra("Direct").toString()
        Log.d("qiqiqi", D)
        readData()


        showNotificationBtn = binding.setbtn
        showNotificationBtn.setOnClickListener {

            showNotification()
            saveData()
            timer.cancel()
            val i = Intent(this, MyService::class.java)
            startService(i)
        }
        stopNameTextView = findViewById(R.id.stopNameTextView)
        nextBusTimeTextView = findViewById(R.id.nextBusTimeTextView)
        BBus = findViewById(R.id.BBus)
        n = findViewById(R.id.n)



        var stopName = intent.getStringExtra("stopName")
        var remainingTime = intent.getStringExtra("nextBusTime")
        var firstStopName = intent.getStringExtra("firstStopName")
        var Firstnameee = intent.getStringExtra("Firstnameee")
        var BusID = intent.getStringExtra("BusID")
        var BusD = intent.getStringExtra("BusD")

        if (!stopName.isNullOrEmpty()) {
            stopNameTextView.text = stopName
            stop = stopName
        } else {
            stopNameTextView.text = stop
        }

        if (!remainingTime.isNullOrEmpty()) {
            nextBusTimeTextView.text = remainingTime
            time = remainingTime
        } else{
            nextBusTimeTextView.text = time
        }

        if (!BusID.isNullOrEmpty()) {
            n.text = "$BusID 號"
            routeNumber = "$BusID 號"
        } else if (!BusD.isNullOrEmpty()) {
            n.text = "$BusD 號"
            routeNumber = "$BusD 號"
        } else {
            n.text = routeNumber
        }




        if (!firstStopName.isNullOrEmpty()) {
            BBus.text = "往 $firstStopName"
            direction = "往  $firstStopName"
        } else if (!Firstnameee.isNullOrEmpty()) {
            BBus.text = "往 $Firstnameee"
            direction = "往  $Firstnameee"
        } else {
            BBus.text = direction
        }


        clickButton = binding.btnBusSearch
        clickButton.setOnClickListener(View.OnClickListener {
            val intent = Intent()
            timer.cancel()
            intent.setClass(this, MainMenu::class.java)
            startActivity(intent)
        })
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = sharedPreferences.getString(KEY_USERNAME, "")
        val textView6 = findViewById<TextView>(R.id.textView6)
        if (!name.isNullOrEmpty()) {
            textView6.text = name
        }
        Log.d("jbjq", sharedPreferences.getString(KEY_USERNAME, "").toString())

        val savedReminderTime = sharedPreferences.getInt("reminderTime", 0)
        reminderTime = savedReminderTime
        binding.sets.text = savedReminderTime.toString()
//        binding.sets.text = reminderTime.toString()
        binding.set.setOnClickListener {
//            val currentReminderTime = sharedPreferences.getInt("reminderTime",0)
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Setting the ReminderTime")
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            input.setText(reminderTime.toString())
            dialog.setView(input)
            dialog.setPositiveButton("Store") { _, _ ->
                val newReminderTime = input.text.toString().toIntOrNull()
                if (newReminderTime != null) {
                    val editor = sharedPreferences.edit()
                    editor.putInt("reminderTime", newReminderTime)
                    editor.apply()
                    Toast.makeText(this, "提醒時間已儲存", Toast.LENGTH_SHORT).show()
                    reminderTime = newReminderTime
                    binding.sets.text = newReminderTime.toString()
                } else {
                    Toast.makeText(this, "輸入的提醒時間無效", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.setNegativeButton("Cancel", null)
            dialog.show()

        }


        val fromPage = intent.getStringExtra("fromPage")

        val goMenuButton: ImageButton = findViewById(R.id.gogomenu)
        goMenuButton.setOnClickListener {
            when (fromPage) {
                "Busleft" -> {
                    val intent = Intent(this, Busleft::class.java)
                    startActivity(intent)
                }

                "Busright" -> {
                    val intent = Intent(this, Busright::class.java)
                    startActivity(intent)
                }

                "MainMenu" -> {
                    val intent = Intent(this, MainMenu::class.java)
                    startActivity(intent)
                }
            }
        }

        goMenuButton.setBackgroundResource(R.drawable.baseline_arrow_back_24)




        timer.schedule(object : TimerTask() {
            override fun run() {
                getData()
            }
        }, 0, 10000)




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
                        Log.d("QAQ", D)
                        response.body()?.forEach {
                            if (it.Direction == D &&
                                it.RouteName.Zh_tw == n.text.toString().substring(0, 2) &&
                                it.StopName.Zh_tw == stopNameTextView.text.toString()
                            ) {

                                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                val dateTime = OffsetDateTime.parse(it.NextBusTime, formatter)
                                val localDateTime = dateTime.toLocalDateTime()
                                val currentTime = LocalDateTime.now()
                                if (localDateTime.isAfter(currentTime)) {
                                    var minutesRemaining =
                                        Duration.between(currentTime, localDateTime).toMinutes()

                                    Log.d("hhd", localDateTime.toString())
                                    if(!(it.Estimates.isNullOrEmpty())){
                                        Log.d("ddd",it.Estimates[0].EstimateTime)
                                        minutesRemaining=it.Estimates[0].EstimateTime.toLong() / 60
                                        if (minutesRemaining <= 1) {
                                            nextBusTimeTextView.text = "即將到站"
                                            time = "即將到站"
                                        } else if (minutesRemaining > 60) {
                                            val hours = minutesRemaining / 60
                                            val minutes = minutesRemaining % 60
                                            val estimateText = "${hours}hr ${minutes}mins"
                                            nextBusTimeTextView.text = estimateText
                                            time = estimateText
                                        } else {
                                            val estimateText = "${minutesRemaining}mins"
                                            nextBusTimeTextView.text = estimateText
                                            time = estimateText
                                        }

                                    }else if (minutesRemaining <= 0) {
                                        nextBusTimeTextView.text = "即將到站"
                                        time = "即將到站"
                                    } else if (minutesRemaining > 60) {
                                        val hours = minutesRemaining / 60
                                        val minutes = minutesRemaining % 60
                                        val estimateText = "${hours}hr ${minutes}mins"
                                        nextBusTimeTextView.text = estimateText
                                        time = estimateText
                                    } else {
                                        val estimateText = "${minutesRemaining}mins"
                                        nextBusTimeTextView.text = estimateText
                                        time = estimateText
                                    }
                                    Log.d("hfkh", binding.nextBusTimeTextView.text.toString()+" "+minutesRemaining)
                                } else {
                                    nextBusTimeTextView.text = "末班已過"
                                    time = "末班已過"
                                }
                            }
                        }
                    }
                }
            })


    }

    private fun showNotification() {
        createNotificationChannel()
        val vibrateEffect: LongArray = longArrayOf(3000, 3000, 3000, 3000)
        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.CHINESE).format(date).toInt()
        val stopName = stopNameTextView.text.toString()
        val remainingTime = nextBusTimeTextView.text.toString()
        val reminderTime = binding.sets.text.toString().toIntOrNull() ?: 0
        val notificationText =
            "$stopName 抵達時間為 $remainingTime ，將於抵達 $reminderTime 分鐘前提醒您"
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("成功設置")
            .setContentText(notificationText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(vibrateEffect)
            .setLights(Color.GRAY, 1000, 1000)


        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MyNotification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            val description = "My notification channel description"
            notificationChannel.description = description
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)


        }
    }

    private fun readData() {
        routeNumber =
            getSharedPreferences("Data", 0).getString("routeNumber", "尚無資料").toString()
        direction =
            getSharedPreferences("Data", 0).getString("direction", "尚無資料").toString()
        stop = getSharedPreferences("Data", 0).getString("stop", "尚無資料").toString()
        time = getSharedPreferences("Data", 0).getString("time", "尚無資料").toString()
        D = getSharedPreferences("Data", 0).getString("Direct", "尚無資料").toString()
        Log.d("DD_ReadData", D)

    }

    private fun saveData() {
        val sharPref = getSharedPreferences("Data", 0)
        sharPref.edit()
            .putString("routeNumber", routeNumber)
            .putString("direction", direction)
            .putString("stop", stop)
            .putString("time", time)
            .putString("Direct", D)
            .putString("sets", binding.sets.text.toString())

            .apply()
    }

}