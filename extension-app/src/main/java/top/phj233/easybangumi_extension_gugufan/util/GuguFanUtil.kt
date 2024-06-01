package top.phj233.easybangumi_extension_gugufan.util

import android.util.Log
import com.heyanle.easybangumi4.source_api.entity.CartoonImpl
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class GuguFanUtil {

    var url: String = "https://www.gugufan.com"
    private var videoUrl: String = "https://www.gugufan.com/index.php/vod/detail/id/"
    private var userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"

    fun getRecentUpdate(): Elements {
        val recentUpdateDocument = Jsoup.connect("$url/index.php/map/index.html").userAgent(userAgent).get()
        val recentUpdateElement = recentUpdateDocument.getElementsByClass("public-list-box public-pic-b [swiper]")
        if (recentUpdateElement.size == 0) {
            Log.e("GuguFan", "获取最近更新失败")
            throw Exception("获取最近更新失败")
        }
        return recentUpdateElement
    }

    private fun getVideoPageDocById(id: String): Document {
        return Jsoup.connect("$videoUrl$id.html").userAgent(userAgent).get()
    }

    fun getCartoonDetailById(id: String): CartoonImpl {
        val videoDocument = getVideoPageDocById(id)
        val title = videoDocument.getElementsByClass("slide-info-title hide").text()
        val coverUrl = videoDocument.getElementsByClass("detail-pic")[0].getElementsByTag("img").attr("data-src")
        val intro = videoDocument.getElementsByClass("text cor3")[0].text()
        val tag = videoDocument.getElementsByClass("slide-info hide").last()!!.getElementsByTag("a")
            .joinToString(",") { it.text().trim() }

        return CartoonImpl(
            id = id,
            title = title,
            coverUrl = coverUrl,
            description = intro,
            genre = tag,
            url = "$videoUrl$id.html",
            source = null.toString()
        )
    }

    fun getPlayLineById(id: String): List<PlayLine> {
        val playLines = arrayListOf<PlayLine>()
        val videoDocument = getVideoPageDocById(id)
        val episodes = arrayListOf<Episode>()
        val playLabel = videoDocument.selectXpath("/html/body/div[5]/div[2]/div[1]/div/a")
            .text().trim()
        videoDocument.getElementsByClass("box border").forEachIndexed { index, it ->
            val episodeElement = it.getElementsByTag("a")
            val regex = Regex("nid/(\\d+)")
            val episodeId = regex.find(episodeElement.attr("href"))!!.groupValues[1]
            val episodeOrder = index + 1
            val episodeLabel = episodeElement.text().trim()
            episodes.add(
                Episode(
                    id = episodeId,
                    order = episodeOrder,
                    label = episodeLabel,
                )
            )
        }
        PlayLine(
            id = id,
            episode = episodes,
            label = playLabel
        ).let {
            playLines.add(it)
        }

        return playLines
}


}
