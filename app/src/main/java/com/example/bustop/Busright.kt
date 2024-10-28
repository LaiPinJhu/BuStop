package com.example.bustop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Response
import com.example.bustop.databinding.ActivityBusrightBinding
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class Busright : AppCompatActivity() {
    private lateinit var clickButton: Button
    private lateinit var binding: ActivityBusrightBinding
    private lateinit var leftListView1: RecyclerView
    private lateinit var adapter1: MainAdapter
    private var busList1: MutableList<BusData> = mutableListOf()
    private lateinit var routeNameTextView1: TextView
    private var busLi1: MutableList<BusData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusrightBinding.inflate(layoutInflater)
        setContentView(binding.root)


        leftListView1 = binding.BusData1

        routeNameTextView1 = binding.busnumber

        val v2 = intent.getStringExtra("value")
        routeNameTextView1.text = v2

//        val intent = Intent(this, BusReminder::class.java)
//        intent.putExtra("busnumber", v2.toString())
//        startActivity(intent)


        clickButton = binding.btnLef
        clickButton.setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.setClass(this, Busleft::class.java)
            startActivity(intent)
        })



        val mainMenuButton: ImageButton = findViewById(R.id.gomenu)
        mainMenuButton.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
        mainMenuButton.setBackgroundResource(R.drawable.baseline_arrow_back_24)

        Timer().schedule(object : TimerTask(){
            override fun run(){
                busList1.clear()
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
                        response.body()?.forEach {
                            Log.d("api", it.toString())
                            if (it.Direction == "1" && it.RouteName.Zh_tw == "12") {
                                busList1.add(it)
                            }
                            if (it.Direction == "0" && it.RouteName.Zh_tw == "12") {
                                busLi1.add(it)
                            }
                        }
                        busList1.sortBy { it.StopSequence.toInt() }
                        busLi1.sortBy { it.StopSequence.toInt() }

                        val lastStopSequence2 = busList1.lastOrNull()?.StopSequence
                        val lastStopSequence3 = busLi1.lastOrNull()?.StopSequence


                        adapter1 = MainAdapter(busList1)
                        leftListView1.layoutManager = LinearLayoutManager(this@Busright)
                        leftListView1.adapter = adapter1

                        if (busList1.isNotEmpty()) {
                            routeNameTextView1.text = busList1[0].RouteName.Zh_tw

                        val firstStopName1 = busList1.find { it.StopSequence == lastStopSequence2 }?.StopName?.Zh_tw
                        if (!firstStopName1.isNullOrEmpty()) {
                            binding.btnRight1.text = firstStopName1
                        }
                        val rouStopName =  busLi1.find { it.Direction == "0" && it.StopSequence == lastStopSequence3 }?.StopName?.Zh_tw
                        if(!rouStopName.isNullOrEmpty()) {
                            binding.btnLef.text = rouStopName
                            }
                        }


                    }
                }
            })
    }

    private class MainAdapter(private val list: MutableList<BusData>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var firstStopName: String? = null
        val lastStopSequence2 = list.lastOrNull()?.StopSequence


        init {
            val firstStopData = list.find { it.StopSequence == lastStopSequence2 && it.Direction == "1"}
            firstStopName = firstStopData?.StopName?.Zh_tw
        }



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listhand, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val uvHolder = (holder as ItemViewHolder)
            uvHolder.setStopName1(list[position].StopName.Zh_tw)
//            val nbT = list[position]?.NextBusTime.toString().substring(11,16) ?: "末班車已過"


//            val nbT = if (list[position]?.NextBusTime != null) {
//                list[position]?.NextBusTime.toString().substring(11, 16)
//            } else {
//                "末班已過"
//            }
//            uvHolder.setNextBusTime1(nbT)



            val nbT = list[position].NextBusTime
            var minutesRemaining:Long = 0
            var estimateText=""
            if(!(list[position].Estimates.isNullOrEmpty())){
                Log.d("ddd",list[position].Estimates[0].EstimateTime)
                minutesRemaining=list[position].Estimates[0].EstimateTime.toLong() / 60
                if (minutesRemaining <= 1) {
                    uvHolder.setNextBusTime1("即將到站")
                    estimateText="即將到站"
                } else if (minutesRemaining > 60) {
                    val hours = minutesRemaining / 60
                    val minutes = minutesRemaining % 60
                    estimateText = "${hours}hr ${minutes}mins"
                    uvHolder.setNextBusTime1(estimateText)
                } else {
                    estimateText = "${minutesRemaining}mins"
                    uvHolder.setNextBusTime1(estimateText)
                }

            }else if (nbT != null) {
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val dateTime = OffsetDateTime.parse(nbT, formatter)

                // 將時間轉換為當地時區
                val localDateTime = dateTime.toLocalDateTime()

                // 取得當前時間
                val currentTime = LocalDateTime.now()

                // 比較當前時間和到站時間
                if (localDateTime.isAfter(currentTime)) {
                    // 計算剩餘時間
                    minutesRemaining = Duration.between(currentTime, localDateTime).toMinutes()

                    if (minutesRemaining <= 0) {
                        uvHolder.setNextBusTime1("即將到站")
                        estimateText="即將到站"
                    } else if (minutesRemaining > 60) {
                        val hours = minutesRemaining / 60
                        val minutes = minutesRemaining % 60
                        estimateText = "${hours}hr ${minutes}mins"
                        uvHolder.setNextBusTime1(estimateText)
                    } else {
                        estimateText = "${minutesRemaining}mins"
                        uvHolder.setNextBusTime1(estimateText)
                    }
                } else {
                    uvHolder.setNextBusTime1("末班已過")
                    estimateText="末班已過"
                }
            } else {
                uvHolder.setNextBusTime1("末班已過")
                estimateText="末班已過"
            }



            holder.itemView.setOnClickListener {
//                val intent = Intent(holder.itemView.context, BusReminder::class.java)
                val intent = Intent(holder.itemView.context.applicationContext, BusReminder::class.java)
                intent.putExtra("stopName", list[position].StopName.Zh_tw)
                intent.putExtra("nextBusTime", estimateText )
                intent.putExtra("Firstnameee", firstStopName)
                intent.putExtra("BusD", list[position].RouteID)
                intent.putExtra("fromPage", "Busright")
                intent.putExtra("Direct", "1")
                holder.itemView.context.startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

        private class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var stopNameTextView1: TextView = itemView.findViewById(R.id.StopName2)
            var estimateTextView1: TextView = itemView.findViewById(R.id.Estimate2)

            fun setStopName1(stopName: String) {
                stopNameTextView1.text = stopName
            }

            fun setNextBusTime1(nextBusTime: String) {
                estimateTextView1.text = nextBusTime
            }

        }
    }
}