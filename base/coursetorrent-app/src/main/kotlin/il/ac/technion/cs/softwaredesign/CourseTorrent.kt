package il.ac.technion.cs.softwaredesign

import CompareTwoKnwonPeersAsString
import GetFirstUrlSucessInterval
import KnowPeerAsString
import ListOfListOfStringToListOfString

import Parser
import SHA1hash
import byteArrayToListOfListOfString
import com.google.inject.Inject
import il.ac.technion.cs.softwaredesign.exceptions.TrackerException
import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory
import lib_delete
import lib_read
import lib_write


import listOfListOfStringToByteArray
import java.lang.Exception
import java.net.URL

/**
 * This is the class implementing CourseTorrent, a BitTorrent client.
 *
 * Currently specified:
 * + Parsing torrent metainfo files (".torrent" files)
 * + Communication with trackers (announce, scrape).
 */

class CourseTorrent @Inject constructor(private val factory : SecureStorageFactory) {

    var announe_list_library = factory.open("announce_list".toByteArray())
    var KnowPeer_library = factory.open("KnownPeer".toByteArray())
    var ScrapeData_library = factory.open("ScrapeData".toByteArray())



    /**
     * Load in the torrent metainfo file from [torrent]. The specification for these files can be found here:
     * [Metainfo File Structure](https://wiki.theory.org/index.php/BitTorrentSpecification#Metainfo_File_Structure).
     *
     * After loading a torrent, it will be available in the system, and queries on it will succeed.
     *
     * This is a *create* command.
     *
     * @throws IllegalArgumentException If [torrent] is not a valid metainfo file.
     * @throws IllegalStateException If the infohash of [torrent] is already loaded.
     * @return The infohash of the torrent, i.e., the SHA-1 of the `info` key of [torrent].
     */
    fun load(torrent: ByteArray): String {
        val parser = Parser(torrent) // @throws IllegalStateException If the infohash of [torrent] is already loaded.

        val metainfomap = parser.metaInfoMap
        val infohash = parser.infohash

        val readVal = lib_read(infohash,announe_list_library)

        if (readVal != null && !readVal.toString(Charsets.UTF_8).equals("0"))//already exists and not deleted
            throw IllegalStateException()



        val announce = metainfomap.get("announce-list") as List<List<String>>?

        if(announce == null){
            val list = ArrayList<String>()
            list.add( metainfomap.get("announce") as String)
            val list2 = ArrayList<ArrayList<String>>()
            list2.add(list)


            lib_write(infohash, listOfListOfStringToByteArray(list2).toString(Charsets.UTF_8),announe_list_library)
        }
        else
            lib_write(infohash, listOfListOfStringToByteArray(announce).toString(Charsets.UTF_8),announe_list_library)

        return infohash
    }

    /**
     * Remove the torrent identified by [infohash] from the system.
     *
     * This is a *delete* command.
     *
     * @throws IllegalArgumentException If [infohash] is not loaded.
     */
    fun unload(infohash: String): Unit {


        val readVal = lib_read(infohash,announe_list_library)
        if (readVal == null || readVal.toString(Charsets.UTF_8).equals("0"))
            throw IllegalArgumentException()

        lib_delete(infohash,announe_list_library)
        lib_delete(infohash,KnowPeer_library)
        lib_delete(infohash,ScrapeData_library)

    }

    /**
     * Return the announce URLs for the loaded torrent identified by [infohash].
     *
     * See [BEP 12](http://bittorrent.org/beps/bep_0012.html) for more information. This method behaves as follows:
     * * If the "announce-list" key exists, it will be used as the source for announce URLs.
     * * If "announce-list" does not exist, "announce" will be used, and the URL it contains will be in tier 1.
     * * The announce URLs should *not* be shuffled.
     *
     * This is a *read* command.
     *
     * @throws IllegalArgumentException If [infohash] is not loaded.
     * @return Tier lists of announce URLs.
     */
    fun announces(infohash: String): List<List<String>> {


        val readVal = lib_read(infohash,announe_list_library)

        if(readVal == null || readVal.toString(Charsets.UTF_8).equals("0"))
            throw IllegalArgumentException()

        return byteArrayToListOfListOfString( readVal )
    }

    /**
     * Send an "announce" HTTP request to a single tracker of the torrent identified by [infohash], and update the
     * internal state according to the response. The specification for these requests can be found here:
     * [Tracker Protocol](https://wiki.theory.org/index.php/BitTorrentSpecification#Tracker_HTTP.2FHTTPS_Protocol).
     *
     * If [event] is [TorrentEvent.STARTED], shuffle the announce-list before selecting a tracker (future calls to
     * [announces] should return the shuffled list). See [BEP 12](http://bittorrent.org/beps/bep_0012.html) for more
     * information on shuffling and selecting a tracker.
     *
     * [event], [uploaded], [downloaded], and [left] should be included in the tracker request.
     *
     * The "compact" parameter in the request should be set to "1", and the implementation should support both compact
     * and non-compact peer lists.//TODO : THIS
     *
     * Peer ID should be set to "-CS1000-{Student ID}{Random numbers}", where {Student ID} is the first 6 characters
     * from the hex-encoded SHA-1 hash of the student's ID numbers (i.e., `hex(sha1(student1id + student2id))`), and
     * {Random numbers} are 6 random characters in the range [0-9a-zA-Z] generated at instance creation.
     *
     * If the connection to the tracker failed or the tracker returned a failure reason, the next tracker in the list
     * will be contacted and the announce-list will be updated as per
     * [BEP 12](http://bittorrent.org/beps/bep_0012.html).
     * If the final tracker in the announce-list has failed, then a [TrackerException] will be thrown.
     *
     * This is an *update* command.
     *
     * @throws TrackerException If the tracker returned a "failure reason". The failure reason will be the exception
     * message.
     * @throws IllegalArgumentException If [infohash] is not loaded.
     * @return The interval in seconds that the client should wait before announcing again.
     */
    fun announce(infohash: String, event: TorrentEvent, uploaded: Long, downloaded: Long, left: Long): Int {


        var the_announces_list = try{
                                         announces(infohash)
                                 }catch (E: Exception){
                                    throw E
                                 }

        val alphbet : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        val peer_id = "-CS1000-" + SHA1hash("209418441208607507".toByteArray()).substring(0,6) +
                List(6) {alphbet.random()}.joinToString("")

        var shuffled_annouce_list = mutableListOf<List<String>>()
        var KnownPeers = mutableListOf<List<String>>()

        var interval =  GetFirstUrlSucessInterval(the_announces_list,infohash,peer_id,event.name,uploaded,downloaded,left , shuffled_annouce_list,KnownPeers)
        if(interval == -1){
            lib_write(infohash,listOfListOfStringToByteArray(shuffled_annouce_list).toString(Charsets.UTF_8),announe_list_library)
            //TODO : also write known perrs
            throw TrackerException("all the trackers didint work , in all the tiers")
        }else{
            lib_write(infohash,listOfListOfStringToByteArray(shuffled_annouce_list).toString(Charsets.UTF_8),announe_list_library)
            lib_write(infohash,listOfListOfStringToByteArray(KnownPeers).toString(Charsets.UTF_8),KnowPeer_library)

            return interval
        }
    }


    /**
     * Scrape all trackers identified by a torrent, and store the statistics provided. The specification for the scrape
     * request can be found here:
     * [Scrape Protocol](https://wiki.theory.org/index.php/BitTorrentSpecification#Tracker_.27scrape.27_Convention).
     *
     * All known trackers for the torrent will be scraped.
     *
     * This is an *update* command.
     *
     * @throws IllegalArgumentException If [infohash] is not loaded.
     */
    fun scrape(infohash: String): Unit = TODO("Implement me!")

    /**
     * Invalidate a previously known peer for this torrent.
     *
     * If [peer] is not a known peer for this torrent, do nothing.
     *
     * This is an *update* command.
     *
     * @throws IllegalArgumentException If [infohash] is not loaded.
     */
    fun invalidatePeer(infohash: String, peer: KnownPeer): Unit {
        val readVal = lib_read(infohash,KnowPeer_library)
        if (readVal == null || readVal.toString(Charsets.UTF_8).equals("0"))
            throw IllegalArgumentException()

        val KnowPeerAsString = KnowPeerAsString(peer.ip,peer.port,peer.peerId)
        val KnowPeers = byteArrayToListOfListOfString(readVal)
        for(curr in 0..KnowPeers.size) {
            KnowPeers[curr].forEach {
                if (it.equals(KnowPeerAsString)){
                    val Corrected_Know_Peers = KnowPeers.drop(curr)
                }
            }
        }

        lib_write(infohash,listOfListOfStringToByteArray(KnowPeers).toString(Charsets.UTF_8),KnowPeer_library)

    }




    /**
     * Return all known peers for the torrent identified by [infohash], in sorted order. This list should contain all
     * the peers that the client can attempt to connect to, in ascending numerical order. Note that this is not the
     * lexicographical ordering of the string representation of the IP addresses: i.e., "127.0.0.2" should come before
     * "127.0.0.100".
     *
     * The list contains unique peers, and does not include peers that have been invalidated.
     *
     * This is a *read* command.
     *
     * @throws IllegalArgumentException If [infohash] is not loaded.
     * @return Sorted list of known peers.
     */
    fun knownPeers(infohash: String): List<KnownPeer> {
        val readVal = lib_read(infohash,KnowPeer_library)
        if (readVal == null || readVal.toString(Charsets.UTF_8).equals("0"))
            throw IllegalArgumentException()
        val KnowPeers = byteArrayToListOfListOfString(readVal)
        var sorted_peers = KnowPeers.sortedWith(Comparator<List<String>>{
            peer_1, peer_2 ->CompareTwoKnwonPeersAsString(peer_1[0],peer_2[0])
        })

        var sorted_peers_as_list_of_strings = ListOfListOfStringToListOfString(sorted_peers)
        var unique_peers = sorted_peers_as_list_of_strings.distinct()
        var res = ArrayList<KnownPeer>()
        unique_peers.forEach {
            var ip = it.substringBefore(':')
            var port = it.substringAfter(':').substringBefore('-').toInt()
            var peer_ip : String? = if (it.contains('-')){ it.substringAfter('-')}
                                    else{ null}
            res.add(KnownPeer(ip,port,peer_ip))
        }

        return res


    }

    /**
     * Return all known statistics from trackers of the torrent identified by [infohash]. The statistics displayed
     * represent the latest information seen from a tracker.
     *
     * The statistics are updated by [announce] and [scrape] calls. If a response from a tracker was never seen, it
     * will not be included in the result. If one of the values of [ScrapeData] was not included in any tracker response
     * (e.g., "downloaded"), it would be set to 0 (but if there was a previous result that did include that value, the
     * previous result would be shown).
     *
     * If the last response from the tracker was a failure, the failure reason would be returned ([ScrapeData] is
     * defined to allow for this). If the failure was a failed connection to the tracker, the reason should be set to
     * "Connection failed".
     *
     * This is a *read* command.
     *
     * @throws IllegalArgumentException If [infohash] is not loaded.
     * @return A mapping from tracker announce URL to statistics.
     */
    fun trackerStats(infohash: String): Map<String, ScrapeData> = TODO("Implement me!")
}