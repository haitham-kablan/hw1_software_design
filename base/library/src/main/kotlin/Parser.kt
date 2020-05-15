import java.lang.Exception
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*

/**
 * @throws IllegalArgumentException If [str] is not a valid metainfo file.
 */
class Parser public constructor(str : ByteArray) {


    private val torrentFileText = str

    private var pieces_flag: Boolean = false

    private var info_start = 0
    private var info_end = 0
    private var pieces_start = 0
    private var peers_start = 0
    private var peers_end = 0
    private var pieces_end = 0
    private var id : Int = 0

    public val metaInfoMap  = try{
        read() as LinkedHashMap< String , Any?>
    }catch(E:Exception){
        throw IllegalArgumentException()
    }


    public val infohash :String

    init {

        //skip the len , becuase it isnot raw bytes
        while(torrentFileText[pieces_start].toChar() != ':'){
            pieces_start++
        }


        val str1 = torrentFileText.copyOfRange(info_start ,info_end )

        infohash = SHA1hash(str1)

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


    /**
     * @throws IllegalArgumentException If [torrentFileText] is not a valid metainfo file.
     */
    private fun read(): Any? {
        if (id >= torrentFileText.size) {
            throw IllegalArgumentException()
        }



        val type: Char = torrentFileText[id].toChar()
        ++id

        //integer example : i5e
        //returns Int
        if (type == 'i') {
            var out: Int = 0
            val start: Int = id
            val limit: Int = id + 22
            var neg = false
            while (id <= limit) {
                val c: Char = torrentFileText[id].toChar()
                if (id == start && c == '-') {
                    neg = true
                    ++id
                    continue
                }
                if (c == 'e') return if (neg) -out else out
                out = out * 10 + (c.toInt() - 48)
                ++id
            }
        }



        //list:  l element element element e
        //returns ArrayList
        else if (type == 'l') {
            val out = ArrayList<Any?>()
            while (true) {
                if (torrentFileText[id].toChar() == 'e') return out
                out.add(read())
                ++id
            }
        }



        // dictinary d key value key value e
        //Returns LinkedHashMap
        else if (type == 'd') {

            val out = LinkedHashMap<Any?, Any?>()
            while (true) {
                if (torrentFileText[id].toChar() == 'e') return out
                val key = read()
                ++id
                if(key == "peers")
                    peers_start = id
                if(key == "pieces")
                    pieces_start = id
                if(key == "info")
                    info_start = id

                val value = read()

                if(key  == "pieces")
                    pieces_end = id
                if(key  == "peers")
                    peers_end = id
                if(key == "info")
                    info_end = id + 1

                out[key] = value
                ++id
            }

        }

        //string example :  5:hihih
        //returns String or ByteArray
        else if (type in '0'..'9') {
            var len = type.toInt() - 48
            val limit: Int = id + 11
            while (id <= limit) {
                val c: Char = torrentFileText[id].toChar()
                if (c == ':') {

                    if (id in pieces_start..pieces_end)//get out as raw bytes
                    {

                        // val out: String = torrentFileText.toByteArray().decodeToString(id+1,id+len + 1,false)//.substring(id + 1, id  + 11)

                        val out = torrentFileText.copyOfRange(id+1,id+len+1)
                        id +=  len

                        return out
                    }

                    peers_end = if(peers_start!=0){peers_start + len.toString().length + len }
                                else{0}
                    if (id in peers_start..peers_end)//get out as raw bytes
                    {

                        // val out: String = torrentFileText.toByteArray().decodeToString(id+1,id+len + 1,false)//.substring(id + 1, id  + 11)

                        val out = torrentFileText.copyOfRange(id+1,id+len+1)
                        id +=  len
                        peers_start = 0
                        return out
                    }
                    else { //get out as utf8(normal) string

                        val out: String = torrentFileText.copyOfRange(id + 1, id + len + 1).toString(Charsets.UTF_8)
                        id += len
                        return out
                    }
                }
                len = len * 10 + (c.toInt() - 48)
                ++id
            }
        }
        throw IllegalArgumentException()
    }

}