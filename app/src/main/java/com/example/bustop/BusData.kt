package com.example.bustop

data class BusData(
    val PlateNumb: String,
    val StopUID: String,
    val StopID: String,
    val StopName: stopName,
    val RouteUID: String,
    val RouteID: String,
    val RouteName: routeName,
    val SubRouteUID: String,
    val SubRouteID: String,
    val SubRouteName: subRouteName,
    val Direction: String,
    val StopSequence: String,
    val StopStatus: String,
    val NextBusTime: String,
    val SrcUpdateTime: String,
    val UpdateTime: String,
    val Estimates : Array<estimates>
){
    data class stopName(
        val Zh_tw: String,
        val En: String,
    )
    data class routeName(
        val Zh_tw: String,
        val En: String,
    )
    data class subRouteName(
        val Zh_tw: String,
        val En: String,
    )
    data class estimates(
        val EstimateTime: String
    )
}

