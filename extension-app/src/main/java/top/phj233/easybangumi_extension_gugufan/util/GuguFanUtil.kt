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
    private var cartoonUrl: String = "https://www.gugufan.com/index.php/vod/detail/id/"
    private var searchUrl: String = "https://www.gugufan.com/index.php/vod/search.html?wd="
    private var searchPageUrl: String = "https://www.gugufan.com/index.php/vod/search/page/"
    private var playerPageUrl: String = "https://www.gugufan.com/index.php/vod/play/id/"
    private var userAgent: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"
    fun getRecentUpdate(): Elements {
        val recentUpdateDocument = Jsoup.connect("$url/index.php/map/index.html").userAgent(userAgent).get()
        val recentUpdateElement = recentUpdateDocument.getElementsByClass("public-list-box public-pic-b [swiper]")
        if (recentUpdateElement.size == 0) {
            Log.e("GuguFan", "获取最近更新失败")
        }
        return recentUpdateElement
    }

    private fun getCartoonPageDocById(id: String): Document {
        return Jsoup.connect("$cartoonUrl$id.html").userAgent(userAgent).get()
    }

    fun getCartoonDetailById(id: String): CartoonImpl {
        val videoDocument = getCartoonPageDocById(id)
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
            url = "$cartoonUrl$id.html",
            source = null.toString()
        )
    }

    fun getPlayLineById(id: String): List<PlayLine> {
        val cartoonDoc = getCartoonPageDocById(id)
        val playLines = arrayListOf<PlayLine>()
        val episodes = arrayListOf<Episode>()
        val playLabel = cartoonDoc.selectXpath("/html/body/div[5]/div[2]/div[1]/div/a")
            .text().trim()
        cartoonDoc.getElementsByClass("box border").forEachIndexed { index, it ->
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
            id = "1" ,
            episode = episodes,
            label = playLabel
        ).let {
            playLines.add(it)
        }

        return playLines
    }

    fun getPlayerPageHtml(id: String, sid: String, nid: String): String {
        return Jsoup.connect("$playerPageUrl$id/sid/$sid/nid/$nid.html").userAgent(userAgent).get().html()
    }

    fun getResultPageSize(keyword: String): Int {
        val text = Jsoup.connect("$searchUrl$keyword").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
        val regex = Regex("共(\\d+)条")
        val resultCount = regex.find(text)!!.groupValues[1].toInt()
        return if (resultCount % 10 == 0) {
            resultCount / 10
        } else {
            resultCount / 10 + 1
        }
    }

    fun getSearchResult(keyword: String, page: Int): Elements {
        return Jsoup.connect("$searchPageUrl$page/wd/$keyword.html").userAgent(userAgent).get().getElementsByClass("public-list-exp")
    }

}
