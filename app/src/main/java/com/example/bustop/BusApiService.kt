package com.example.bustop

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BusApiService{

    @GET("Taichung/12")
    @Headers("Authorization:Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJER2lKNFE5bFg4WldFajlNNEE2amFVNm9JOGJVQ3RYWGV6OFdZVzh3ZkhrIn0.eyJleHAiOjE3MjY3MTQ1OTksImlhdCI6MTcyNjYyODE5OSwianRpIjoiNWMwZDgwYjQtNTBmZC00ZGZkLTkzMjMtZjg2Y2Y5NzEzZmZjIiwiaXNzIjoiaHR0cHM6Ly90ZHgudHJhbnNwb3J0ZGF0YS50dy9hdXRoL3JlYWxtcy9URFhDb25uZWN0Iiwic3ViIjoiODk5ZDU3N2YtMDNlYS00OWI0LTk3MzAtNWM4NThhYjI4ZjRjIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiczEzMTEwNjEwMjAtOWUyZGRkZTYtZjBiMS00NmM5IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJzdGF0aXN0aWMiLCJwcmVtaXVtIiwibWFhcyIsImFkdmFuY2VkIiwiZ3JlZW5tYWFzIiwidmFsaWRhdG9yIiwiaGlzdG9yaWNhbCIsImJhc2ljIl19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJ1c2VyIjoiOTMzZTMzYTIifQ.HGSvgP2jDfGngHj3g-pVhsqSAbmtXTx51ewGpd8v3C_xYTUt6xJ84uqQG59t9pGtRYgdvdXoeoLvKqHFwuF3QuYk8Ysc92mJQ3auoYeXmlD_psFr5HzOD0k3R7kE6KdkfCROY4eyyCDIaIKyijTUH9m31SidhewwWxEvLPZhB9x05NdVzBCFvvqb-OUA2gDnYSa0YSlBDsSgLTiUNMlg8zsTSWnObbugCNWSusfOItgKqLeLqjswDvjLZx60kHpeeYeGs6z9ndb0FzvTwulcsOHBAYNGrWWjssXnk64fQy7tVgmr0k5pp0NgF3ywMs1KXS4QNwIxLOrRxI_pmGK0sw")
    fun getUV(
        @Query("top") top:String?=null,
        @Query("format")  format:String
    ):Call<List<BusData>>
}

private const val BASE_URL ="https://tdx.transportdata.tw/api/basic/v2/Bus/EstimatedTimeOfArrival/City/"

object UVApi{

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitService:BusApiService= retrofit.create(BusApiService::class.java)
}