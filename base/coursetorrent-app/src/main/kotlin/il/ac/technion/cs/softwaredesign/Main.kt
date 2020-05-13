package il.ac.technion.cs.softwaredesign
import Parser
import com.google.common.io.Resources.getResource
import com.sun.javafx.scene.control.skin.Utils.getResource
import dev.misfitlabs.kotlinguice4.getInstance
import library
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.DecimalFormat

fun main() {

    val alphbet : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val rand6Chars = List(6) {alphbet.random()}.joinToString("")

    val peer_id = "-CS1000-" + SHA1hash("209418441208607507".toByteArray()) +
            List(6) {alphbet.random()}.joinToString("")

    val hexa_peer_id = StringToEscapedHexa(peer_id)

    //val torrent = CourseTorrent()
    val lame = CourseTorrent::class.java.getResource("/lame.torrent").readBytes()
    val infohash = Parser(lame).infohash

   sendGetRequest("http://bttracker.debian.org:6969/announce",
           "5a8062c076fa85e8056451c0d9aa04349ae27909" ,
       peer_id , TorrentEvent.STARTED,0,0,0)


    println(StringToEscapedHexa(peer_id))
    println(peer_id)




}



fun StringToHex(str : String) : String {

    var Hexa_string = ""
    str.forEach { Hexa_string = Hexa_string + Char_to_hexa_string(it) }
    return Hexa_string

}

fun Char_to_hexa_string(char : Char) : String{

    if(         (  (char >= '0' && char <= '9')
                || (char >= 'a' && char <= 'z')
                || (char >= 'A' && char <= 'Z')
                || char == '.'
                || char == '_'
                || char == '-'
                || char == '~')){

        return char.toString()
    }


    val hexString = String.format("%02X", char.toByte())
    return hexString
}

public fun SHA1hash(str1 : ByteArray) : String{

    val HEX_CHARS = "0123456789abcdef"
    val bytes = MessageDigest
        .getInstance("SHA-1")
        .digest(str1)
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
}
fun sendGet() {
    val url = URL("http://www.google.com/")

    with(url.openConnection() as HttpURLConnection) {
        requestMethod = "GET"  // optional default is GET

        println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

        inputStream.bufferedReader().use {
            it.lines().forEach { line ->
                println("this is line : $line")
            }
        }
    }
}

fun infohashToBytesAsString(infohash : String) : String{

    var infohashAsByterArray : String = ""

    for (i in 0 until infohash.length){

        if (i == infohash.length - 1 ){
            return infohashAsByterArray
        }
        var current_hex = infohash[i].toString() + infohash[i+1].toString()
        var current_hex_value = HexaStringToChar(current_hex)
        infohashAsByterArray = infohashAsByterArray + CharToNNformat(current_hex_value)
        i.plus(1)

    }

    return infohashAsByterArray

}

fun CharToNNformat(char : Char) : String{
    if(         (   (char >= '0' && char <= '9')
                    || (char >= 'a' && char <= 'z')
                    || (char >= 'A' && char <= 'Z')
                    || char == '.'
                    || char == '_'
                    || char == '-'
                    || char == '~')){

        return char.toString()
    }


    return "%" + java.lang.Integer.toHexString(char.toInt())
}


fun HexaStringToChar(HexaString : String) : Char{

    val frist_digit = HexaString[0]
    val second_digit = HexaString[1]

    return (getHexaValue(frist_digit) * 16 + getHexaValue(second_digit)).toChar()


}

fun getHexaValue(char : Char) : Int{

    when (char) {
        'A' -> return 10
        'a' -> return 10
        'B' -> return 11
        'b' -> return 11
        'C' -> return 12
        'c' -> return 12
        'D' -> return 13
        'd' -> return 13
        'E' -> return 14
        'e' -> return 14
        'F' -> return 15
        'f' -> return 15
    }

    return char.toInt() - 48
}
fun sendGetRequest(url : String,infohash : String , peer_id : String , event : TorrentEvent, uploaded: Long, downloaded: Long, left: Long) {

    var reqParam = "info_hash"+ "=" + StringToEscapedHexa(infohash)
    reqParam += "&" + "peer_id" + "=" + peer_id
    reqParam += "&" + "uploaded" + "=" + uploaded.toString()
    reqParam += "&" + "downloaded"+ "=" + downloaded.toString()
    reqParam += "&" + "left" + "=" + left.toString()
    reqParam += "&" + "compact" + "=" + "1"
    reqParam += "&" + "event" + "=" + event.name
    reqParam += "&" + "port" + "=" + "6881"

    val mURL = URL("http://bttracker.debian.org:6969/announce?info_hash=Z%80b%c0v%fa%85%e8%05dQ%c0%d9%aa%044%9a%e2y%09&peer_id=-CS1000-349355fE4cS8&port=6882&uploaded=0&downloaded=0&left=0&compact=1&event=started")
    println(mURL)

    with(mURL.openConnection() as HttpURLConnection) {
        // optional default is GET
        requestMethod = "GET"

        println("URL : $url")
        println("Response Code : $responseCode")

        BufferedReader(InputStreamReader(inputStream)).use {
            val response = StringBuffer()

            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            println("Response : $response")
        }
        //TODO EXTRACT INTERVAL AND RTURNS IT
    }
}
fun StringToEscapedHexa(infohash : String) : String{

    var infohashAsByterArray : String = ""
    var i = 0

   while(i < infohash.length - 1){

//        if (i == infohash.length - 2 ){
//            return infohashAsByterArray
//        }
        var current_hex = infohash[i].toString() + infohash[i+1].toString()
        var current_hex_value = HexaStringToChar(current_hex)
        infohashAsByterArray = infohashAsByterArray + CharToNNformat(current_hex_value)
        i = i + 2

    }

    return infohashAsByterArray

}








