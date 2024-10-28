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
import com.example.bustop.databinding.ActivityBusleftBinding
import retrofit2.Call
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask

class Busleft : AppCompatActivity() {

    private lateinit var clickButton: Button
    private lateinit var binding: ActivityBusleftBinding
    private lateinit var leftListView: RecyclerView
    private lateinit var adapter: MainAdapter
    private var busList: MutableList<BusData> = mutableListOf()
    private lateinit var routeNameTextView: TextView
    private var busLi: MutableList<BusData> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusleftBinding.inflate(layoutInflater)
        setContentView(binding.root)
        leftListView = binding.BusData




        clickButton = binding.btnRight
        clickButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Busright::class.java)
            startActivity(intent)
        })


        routeNameTextView = findViewById(R.id.btitle)

        val vl = intent.getStringExtra("value")
        routeNameTextView.text = vl



        val goMenuButton: ImageButton = findViewById(R.id.gotomenu)
        goMenuButton.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
        goMenuButton.setBackgroundResource(R.drawable.baseline_arrow_back_24)

//        binding.getData.setOnClickListener{


        Timer().schedule(object : TimerTask(){
            override fun run(){
                busList.clear()
                getData()
            }
        }, 0, 10000)

    }

    private fun getData() {
        UVApi.retrofitService.getUV(format = "JSON")
            .enqueue(object : retrofit2.Callback<List<BusData>> {
                override fun onFailure(call: Call<List<BusData>>, t: Throwable) {
                    Log.e("ApiFail", t.toString())
                }

                override fun onResponse(
                    call: Call<List<BusData>>,
                    response: Response<List<BusData>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.forEach {
                            Log.d("api", it.toString())
                            if (it.Direction == "0" && it.RouteName.Zh_tw == "12") {
                                busList.add(it)
                            }
                            if (it.Direction == "1" && it.RouteName.Zh_tw == "12"  ) {
                                busLi.add(it)
                            }
                        }

                        busList.sortBy { it.StopSequence.toInt() }
                        busLi.sortBy { it.StopSequence.toInt() }


                        }
                    val lastStopSequence = busList.lastOrNull()?.StopSequence
                    val lastStopSequence1 = busLi.lastOrNull()?.StopSequence

                        adapter = MainAdapter(busList)
                        leftListView.layoutManager = LinearLayoutManager(this@Busleft)
                        leftListView.adapter = adapter

                        if (busList.isNotEmpty()) {
                            routeNameTextView.text = busList[0].RouteName.Zh_tw

                            val firstStopName = busList.find { it.StopSequence == lastStopSequence }?.StopName?.Zh_tw
                            if (!firstStopName.isNullOrEmpty()) {
                                binding.btnLeft.text = firstStopName

                            }


                            val firstStopName2 = busLi.find { it.Direction == "1" && it.StopSequence == lastStopSequence1  }?.StopName?.Zh_tw
                            if (!firstStopName2.isNullOrEmpty()) {
                                binding.btnRight.text = firstStopName2
                        }
                    }
                }
            })
    }
    private class MainAdapter(private val list: MutableList<BusData> ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var firstStopName: String? = null

        val lastStopSequence = list.lastOrNull()?.StopSequence

        init {
            val firstStopData = list.find { it.StopSequence == lastStopSequence && it.Direction == "0"}
            firstStopName = firstStopData?.StopName?.Zh_tw
        }




            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.leftlist, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val uvHolder = (holder as ItemViewHolder)
            uvHolder.setStopName(list[position].StopName.Zh_tw)

//            val nextBusTime =
//                list[position]?.NextBusTime.toString().substring(11, 16) ?: "末班車已過"
//            val nextBusTime = if (list[position]?.NextBusTime != null) {
//                list[position]?.NextBusTime.toString().substring(11, 16)
//            } else {
//                "末班已過"
//            }
//            uvHolder.setNextBusTime(nextBusTime)

            val nextBusTime = list[position].NextBusTime
            var minutesRemaining:Long = 0
            var estimateText=""
            if(!(list[position].Estimates.isNullOrEmpty())){
                Log.d("ddd",list[position].Estimates[0].EstimateTime)
                minutesRemaining=list[position].Estimates[0].EstimateTime.toLong() / 60
                if (minutesRemaining <= 1) {
                    uvHolder.setNextBusTime("即將到站")
                    estimateText="即將到站"
                } else if (minutesRemaining > 60) {
                    val hours = minutesRemaining / 60
                    val minutes = minutesRemaining % 60
                    estimateText = "${hours}hr ${minutes}mins"
                    uvHolder.setNextBusTime(estimateText)
                } else {
                    estimateText = "${minutesRemaining}mins"
                    uvHolder.setNextBusTime(estimateText)
                }

            }else if (nextBusTime != null) {
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val dateTime = OffsetDateTime.parse(nextBusTime, formatter)

                // 將時間轉換為當地時區
                val localDateTime = dateTime.toLocalDateTime()

                // 取得當前時間
                val currentTime = LocalDateTime.now()

                // 比較當前時間和到站時間
                if (localDateTime.isAfter(currentTime)) {
                    // 計算剩餘時間
                    minutesRemaining = Duration.between(currentTime, localDateTime).toMinutes()

                    if (minutesRemaining <= 1) {
                        uvHolder.setNextBusTime("即將到站")
                        estimateText="即將到站"
                    } else if (minutesRemaining > 60) {
                        val hours = minutesRemaining / 60
                        val minutes = minutesRemaining % 60
                        estimateText = "${hours}hr ${minutes}mins"
                        uvHolder.setNextBusTime(estimateText)
                    } else {
                        estimateText = "${minutesRemaining}mins"
                        uvHolder.setNextBusTime(estimateText)
                    }
                } else {
                    uvHolder.setNextBusTime("末班已過")
                    estimateText="末班已過"
                }
            } else {
                uvHolder.setNextBusTime("末班已過")
                estimateText="末班已過"
            }


            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context.applicationContext, BusReminder::class.java)
                intent.putExtra("stopName", list[position].StopName.Zh_tw)
                intent.putExtra("nextBusTime",  estimateText)
                intent.putExtra("firstStopName", firstStopName)
                intent.putExtra("BusID", list[position].RouteID)
                intent.putExtra("fromPage", "Busleft")
                intent.putExtra("Direct", "0")
                holder.itemView.context.startActivity(intent)
                }


        }


        override fun getItemCount(): Int {
            return list.size
        }

        private class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var stopNameTextView: TextView = itemView.findViewById(R.id.StopName)
            var estimateTextView: TextView = itemView.findViewById(R.id.Estimate)

            fun setStopName(stopName: String) {
                stopNameTextView.text = stopName
            }

            fun setNextBusTime(nextBusTime: String) {
                estimateTextView.text = nextBusTime
            }


        }
    }
}
