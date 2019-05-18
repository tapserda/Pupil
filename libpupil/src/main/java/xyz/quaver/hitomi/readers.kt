package xyz.quaver.hitomi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import org.jsoup.Jsoup
import java.net.URL

fun getReferer(galleryID: Int) = "https://hitomi.la/reader/$galleryID.html"

@Serializable
data class GalleryInfo(
    val width: Int,
    val haswebp: Int,
    val name: String,
    val height: Int
)
@Serializable
data class ReaderItem(
    val url: String,
    val galleryInfo: GalleryInfo?
)
typealias Reader = List<ReaderItem>
//Set header `Referer` to reader url to avoid 403 error
fun getReader(galleryID: Int) : Reader {
    val readerUrl = "https://hitomi.la/reader/$galleryID.html"
    val galleryInfoUrl = "https://ltn.hitomi.la/galleries/$galleryID.js"

    try {
        val doc = Jsoup.connect(readerUrl).get()

        val images = doc.select(".img-url").map {
            protocol + urlFromURL(it.text())
        }

        val galleryInfo = ArrayList<GalleryInfo?>()

        galleryInfo.addAll(
            Json(JsonConfiguration.Stable).parse(
                GalleryInfo.serializer().list,
                Regex("""\[.+]""").find(
                    URL(galleryInfoUrl).readText()
                )?.value ?: "[]"
            )
        )

        if (images.size > galleryInfo.size)
            galleryInfo.addAll(arrayOfNulls(images.size - galleryInfo.size))

        return (images zip galleryInfo).map {
            ReaderItem(it.first, it.second)
        }
    } catch (e: Exception) {
        return emptyList()
    }
}