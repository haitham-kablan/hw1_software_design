import com.google.inject.Inject
import com.sun.javafx.util.Utils.split
import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory
import il.ac.technion.cs.softwaredesign.storage.impl.SecureStorageImpl
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.CharsetDecoder
import java.security.MessageDigest
import java.util.LinkedHashMap


public fun listOfListOfStringToByteArray( list : List<List<String>>) : ByteArray {

    var size = 2 + 2*list.size //2 for l and e of the main list, and another 2 for every secondary list in it


    //calculating the size of theByte array for eachb String
    list.forEach {
        it.forEach {
            val len = it.toByteArray().size
            size += len.toString().length + 1 // space it takes to save the length and then the ':'

            size += len  //the length of the string

        }
    }

    val out = ByteArray(size)
    var i : Int = 0

    out[0] = 'l'.toByte()
    ++i
    list.forEach {

        out[i] = 'l'.toByte()
        ++i
        it.forEach {

            val len = it.toByteArray().size

            len.toString().toByteArray().copyInto(out , i )
            i+=len.toString().length

            out[i] = ':'.toByte()
            ++i

            it.toByteArray().copyInto(out,i)
            i+=len
        }
        out[i] = 'e'.toByte()
        ++i
    }
    out[i] = 'e'.toByte()
    ++i

    return out
}



//assumes the form of list is := "ll4:name5helloel5:name26hello2ee   =>
//                                               <
//                                                  <name,hello>
//                                                  <name2,hello2>
//if not, @throws IllegalArgumentException                                                                  >
public fun byteArrayToListOfListOfString(byteArray : ByteArray) : List<List<String>> {

    if(byteArray[0] != 'l'.toByte())
        throw IllegalArgumentException()

    var outerList = ArrayList<ArrayList<String>>()
    var i = 1


    while (byteArray[i] != 'e'.toByte()){

        if(byteArray[i] != 'l'.toByte())
            throw IllegalArgumentException()

        var innerList = ArrayList<String>()
        ++i

        while (byteArray[i] != 'e'.toByte()){

            var j = i
            while (byteArray[j] != ':'.toByte()){
                j++
            }


            val lengthOfString = ByteArray(j-i)
            var x : Int  = i

            while (x < j){
                lengthOfString[x-i] = byteArray[x]
                x++
            }

            val len =lengthOfString.toString(Charsets.UTF_8).toInt()

            i=j+1
            val strAsBytes = ByteArray(len)
            byteArray.copyInto(strAsBytes,0, i ,i+len)
            i+=len

            innerList.add(strAsBytes.toString(Charsets.UTF_8))

        }
        outerList.add(innerList)
        i++
    }

    return outerList
}


//fun getInterval(old_list : List<List<String>> , infohas : String , peer_id : String,
//                event: String, uploaded: Long,
//                downloaded: Long, left: Long) : Int {
//
//    var list1 = mutableListOf<List<String>>()
//
//
//    for (i in old_list.indices){
//
//        var list = old_list[i]
//        if (Started){
//            list  = old_list[i].shuffled()
//        }
//
//        var list2 = mutableListOf<String>()
//        list.forEach {
//            //sendGetRequest(URL(it) , infohas ,)
//            print(it)
//            // if(SENDING IT TO HTTP WAS SUCCESS)
//            //list2.add(0, it)
//            // else
//            list2.add(it)
//        }
//
//        //list1 is same order as old_list
//        list1.add(list2)
//    }
//    //TODO: write list 1 to db
//    //return list1;
//    return 0
//}



fun GetFirstUrlSucessInterval(old_list : List<List<String>>, infohas : String, peer_id : String,
                              event: String, uploaded: Long,
                              downloaded: Long, left: Long,
                              list_to_write : MutableList<List<String>>,
                               KnownPeers :MutableList<List<String>>) : Int{

    var new_announce_list = ArrayList<ArrayList<String>>()
    var not_found = true
    var interval = 0
    old_list.forEach {

        if (not_found) {
            var shuffled_list = it
            if (event.equals("started")) {
                shuffled_list = it.shuffled()
            }
            var shuffled_list_orderd = ArrayList<String>()


            for (curr in shuffled_list.indices) {
                var response = SendHttpRequset(shuffled_list[curr] , infohas , peer_id , event
                                ,uploaded,downloaded,left)
                    if (CheckResponse(response)) {
                        shuffled_list_orderd = ListAddFirst(shuffled_list[curr], shuffled_list_orderd) as ArrayList<String>

                    //copy rest of the list as it
                    for (i in curr + 1..shuffled_list.size - 1) {
                        shuffled_list_orderd.add(shuffled_list[i])
                    }
                        var cpy_KnownPeers = GetKnownPeers(Parser(response).metaInfoMap.get("peers") as ByteArray)
                        cpy_KnownPeers.forEach{
                            KnownPeers.add(it)
                        }
                        not_found = false
                        interval = ExtractIntervalFromResponse(response)
                        break

                     }
                shuffled_list_orderd = ListAddFirst(shuffled_list[curr], shuffled_list_orderd)  as ArrayList<String>
            }
            new_announce_list.add(shuffled_list_orderd)
        } else {

            var curr_res: ArrayList<String> = ArrayList<String>(it)
            new_announce_list.add(curr_res)
        }

    }
    //WRITE LIST TO DB

    //TODO WRITE THE INFO TO THE DB
    if(not_found){
        interval = -1
    }
    for (curr in new_announce_list.indices) {
        list_to_write.add(new_announce_list[curr])
    }
    return interval

}

fun CheckResponse(response : ByteArray) : Boolean{

    //catched exception
    if(response.size == 0) return false
    if (Parser(response).metaInfoMap.containsKey("failure reason")) return false

    var x = (Parser(response).metaInfoMap.get("peers") as ByteArray).toUByteArray()
    var i = 0

    return true

}

fun GetKnownPeers(byteArray: ByteArray) : MutableList<List<String>>{

    var res  = mutableListOf<List<String>>()
    var not_compat_reponse = try { Parser(byteArray)}
                             catch(E : java.lang.Exception){
                                  res  = mutableListOf<List<String>>()
                                 var unsigned = byteArray.toUByteArray()
                                 var i = 0
                                 while(i < unsigned.size){
                                     var tmp = ArrayList<String>()
                                     tmp.add(ExtractIpAdressWithPort(unsigned.copyOfRange(i,i+6)))
                                     res.add(tmp)
                                     i = i + 6
                                 }

                                 return res
                             }

    //Parase it as non_compact_repsone

    (not_compat_reponse.metaInfoMap.get("peers") as ArrayList<LinkedHashMap<String, Any?>>).forEach {
        var curr_known_peer = (it.get("ip")) as String +
                             ":" + (it.get("port") as Int).toString() +
                                "-" + (it.get("peer id")) as String
        var tmp = ArrayList<String>()
        tmp.add(curr_known_peer)
        res.add(tmp)
    }

    return res

}

fun SendHttpRequset(url: String , infohas : String , peer_id : String,
                              event: String, uploaded: Long,
                              downloaded: Long, left: Long) : ByteArray{

    var response : ByteArray = try {
        sendGetRequest(url, infohas, peer_id, event, uploaded, downloaded, left)
    }catch (E:Exception){
        ByteArray(0)
    }

    return response

}
fun ListAddFirst(item : String, list : List<String> ) : List<String>{

    var returned_list = ArrayList<String>()
    returned_list.add(item)
    list.forEach{
        returned_list.add(it)

    }
    return returned_list
}
fun CharToNNformat(char : Char , curr_hex : String) : String{
    if(         (   (char >= '0' && char <= '9')
                || (char >= 'a' && char <= 'z')
                || (char >= 'A' && char <= 'Z')
                || char == '.'
                || char == '_'
                || char == '-'
                || char == '~')){

        return char.toString()
    }


    return "%" + curr_hex
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
fun sendGetRequest(url : String, infohash : String, peer_id : String, event : String, uploaded: Long,
                   downloaded: Long, left: Long) : ByteArray{

    var reqParam = "info_hash"+ "=" + StringToEscapedHexa(infohash)
    reqParam += "&" + "peer_id" + "=" + peer_id
    reqParam += "&" + "uploaded" + "=" + uploaded.toString()
    reqParam += "&" + "downloaded"+ "=" + downloaded.toString()
    reqParam += "&" + "left" + "=" + left.toString()
    reqParam += "&" + "compact" + "=" + "1"
    reqParam += "&" + "event" + "=" + event
    reqParam += "&" + "port" + "=" + "6881"

    val mURL = URL(url + "?" + reqParam)

    //println(mURL)
    with(mURL.openConnection() as HttpURLConnection) {
        // optional default is GET
        requestMethod = "GET"

        //println("URL : $url")
        //println("Response Code : $responseCode")

        var x = inputStream.readBytes()
        return x

    }


}

fun StringToEscapedHexa(infohash : String) : String{

    var infohashAsByterArray : String = ""
    var i = 0

    while(i < infohash.length - 1){

        var current_hex = infohash[i].toString() + infohash[i+1].toString()
        var current_hex_value = HexaStringToChar(current_hex)
        infohashAsByterArray = infohashAsByterArray + CharToNNformat(current_hex_value,current_hex)
        i = i + 2

    }

    return infohashAsByterArray

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

fun ExtractIntervalFromResponse(response:ByteArray) : Int{

    return (Parser(response).metaInfoMap.get("interval") as Int).toInt()


}


fun ExtractIpAdressWithPort(byteArray: UByteArray) : String{


    return  byteArray[0].toString() + "." +
                        byteArray[1].toString() + "." +
                        byteArray[2].toString() + "." +
                        byteArray[3].toString() + ":" +
            (byteArray[4].toString().toInt() * 256 +  byteArray[5].toString().toInt()).toString()


}


fun KnowPeerAsString( ip: String,
                                  port: Int,
                                  peerId: String?) : String{


    var knownPeer = ip + ":" + port
    if (peerId != null){
        knownPeer = knownPeer + "-" + peerId
    }

    return knownPeer
}

fun IPStringTOIntList(ip : String) : List<Int>{

    var list = ArrayList<Int>()
    (ip as CharSequence).split(",").forEach {
        list.add(it.toInt())
    }

    return list
}

fun CompareTwoKnwonPeersAsString(peer1 : String, peer2 : String) : Int {

    var peer1_list = IPStringTOIntList(peer1)
    var peer2_list = IPStringTOIntList(peer2)

    var i = 0
    while ( i < peer1_list.size ){
        if(peer1_list[i] == peer2_list[i]){
            continue
        }
        return peer1_list[i] - peer2_list[i]
    }

    return 0

}

fun ListOfListOfStringToListOfString(given : List<List<String>>) : List<String>{

    var list = ArrayList<String>()
    given.forEach {
        it.forEach {
            list.add(it)
        }
    }
    return list

}

fun lib_read(key: String , storage : SecureStorage) :ByteArray?{
           return storage.read(key.toByteArray(Charsets.UTF_8));
}

fun lib_write(key: String, value: String , storage : SecureStorage) : Unit{

         storage.write(key.toByteArray(Charsets.UTF_8),value.toByteArray(Charsets.UTF_8));
}

// @throws IllegalArgumentException If [infohash] is not loaded.
fun lib_delete(key: String , storage : SecureStorage) : Unit {
            lib_write(key, "0",storage)
}

//class library : SecureStorage {
//
//    public fun lib_read(key: String ) :ByteArray?{
//            return read(key.toByteArray(Charsets.UTF_8));
//        }
//
//        public fun lib_write(key: String, value: String) : Unit{
//
//            write(key.toByteArray(Charsets.UTF_8),value.toByteArray(Charsets.UTF_8));
//        }
//
//
//        // @throws IllegalArgumentException If [infohash] is not loaded.
//        public fun lib_delete(key: String) : Unit {
//
//            this.lib_write(key, "0")
//
//        }
//
//    override fun read(key: ByteArray): ByteArray? {
//        TODO("Not yet implemented")
//    }
//
//    override fun write(key: ByteArray, value: ByteArray) {
//        TODO("Not yet implemented")
//    }
//
//
//}