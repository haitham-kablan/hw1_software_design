package il.ac.technion.cs.softwaredesign

sealed class ScrapeData {
    //TODO: make properties with getters of this kind
    val name : String
        get() = when(this){
            is ScrapeData -> (this as Scrape)).name
            is Failure -> (this as Failure).reason

        }

}

data class Scrape : ScrapeData (this.complete, this.downloaded, this.incomplete, this.name)(
    val complete: Int,
    val downloaded: Int,
    val incomplete: Int,
    val name: String?

) : ScrapeData()

data class Failure : ScrapeData(
    val reason: String = "scrape not supported"
) : ScrapeData()