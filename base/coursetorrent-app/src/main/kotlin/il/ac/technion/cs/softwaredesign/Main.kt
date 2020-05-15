package il.ac.technion.cs.softwaredesign

import ExtractIntervalFromResponse
import GetFirstUrlSucessInterval
import SHA1hash
import StringToEscapedHexa

import sendGetRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


fun main() {

//    println(StringToEscapedHexa("5a8062c076fa85e8056451c0d9aa04349ae27909"))
//    val alphbet : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
//
//    val peer_id = "-CS1000-" + SHA1hash("209418441208607507".toByteArray()).substring(0,6) +
//                List(6) {alphbet.random()}.joinToString("")
//
//    val res = sendGetRequest("http://bttracker.debian.org:6969/announce" , "5a8062c076fa85e8056451c0d9aa04349ae27909"
//    ,peer_id,TorrentEvent.STARTED.name,0,0,0)
//
//    ExtractIntervalFromResponse(res)
//    println( ExtractIntervalFromResponse(res))
//
    var infohash = "5a8062c076fa85e8056451c0d9aa04349ae27909"
    var teir1 = ArrayList<String>()
    var teir2 = ArrayList<String>()
    teir1.add("http://bttracker.debian.2org:6969/announce")
    teir1.add("2")
    teir1.add("http://bttracke123r.debian.org:6969/announce")
    teir1.add("http://bttracker123.debian.org:6969/announce")
    teir1.add("http://bttrac123ker.debian.org:6969/announce")
    teir2.add("http://bttracker.debian.org:6969/announce")
    teir2.add("2")
    teir2.add("http://bttracke123r.debian.org:6969/announce")
    teir2.add("http://bttracker123.debian.org:6969/announce")
    teir2.add("http://bttrac123ker.debian.org:6969/announce")
    var announce_list = ArrayList<ArrayList<String>>()
    announce_list.add(teir1)
    announce_list.add(teir2)
    announce_list.add(teir1)
    announce_list.add(teir1)
    announce_list.add(teir1)
    val alphbet : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    val peer_id = "-CS1000-" + SHA1hash("209418441208607507".toByteArray()).substring(0,6) +
            List(6) {alphbet.random()}.joinToString("")

    var list = mutableListOf<List<String>>()
    var x =
        GetFirstUrlSucessInterval(announce_list,infohash,peer_id,"started",0,0,0,list)
     println(x)








}

fun ExtractIpAdressWithPort(byteArray: ByteArray) : ArrayList<Int>{

    var ip_addr_with_port = ArrayList<Int>()
    byteArray.forEach {
        var u_byte = it.toUByte()
        println(u_byte)

    }
    return ip_addr_with_port
}


