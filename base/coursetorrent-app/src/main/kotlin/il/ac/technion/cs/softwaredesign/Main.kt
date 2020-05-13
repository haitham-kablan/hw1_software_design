package il.ac.technion.cs.softwaredesign

import ExtractIntervalFromResponse
import SHA1hash
import StringToEscapedHexa

import sendGetRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


fun main() {

    println(StringToEscapedHexa("5a8062c076fa85e8056451c0d9aa04349ae27909"))
    val alphbet : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    val peer_id = "-CS1000-" + SHA1hash("209418441208607507".toByteArray()).substring(0,6) +
                List(6) {alphbet.random()}.joinToString("")

    val res = sendGetRequest("http://bttracker.debian.org:6969/announce" , "5a8062c076fa85e8056451c0d9aa04349ae27909"
    ,peer_id,TorrentEvent.STARTED.name,0,0,0)

    ExtractIntervalFromResponse(res)
    println(res)





}


