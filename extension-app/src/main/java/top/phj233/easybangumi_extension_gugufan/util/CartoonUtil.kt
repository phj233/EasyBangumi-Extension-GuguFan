package top.phj233.easybangumi_extension_gugufan.util

import android.util.Log
import com.google.gson.Gson
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.*
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class CartoonUtil(private val webViewHelperV2: WebViewHelperV2, private val stringHelper: StringHelper) {
    var guguUrl: String = "https://www.gugufan.com"
    var cycUrl: String = "https://www.cycanime.com"
    var userAgent: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"

    fun getRecentUpdate(): Elements {
        val recentUpdateDocument = Jsoup.connect("$guguUrl/index.php/map/index.html").userAgent(userAgent).get()
        val recentUpdateElement = recentUpdateDocument.getElementsByClass("public-list-box public-pic-b [swiper]")
        if (recentUpdateElement.size == 0) {
            Log.e("GuguFan", "获取最近更新失败")
        }
        return recentUpdateElement
    }

    private fun getCartoonPageDocById(source: String,id: String): Document {
        return if (source == "gugu") {
            Jsoup.connect("$guguUrl/index.php/vod/detail/id/$id.html").userAgent(userAgent).get()
        } else {
            Jsoup.connect("$cycUrl/bangumi/$id.html").userAgent(userAgent).get()
        }
    }

    fun getCartoonDetailById(source: String,id: String): CartoonImpl {
        val videoDocument = getCartoonPageDocById(source,id)
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
            url = if (source == "gugu") {
                "$guguUrl/index.php/vod/detail/id/$id.html"
            } else {
                "$cycUrl/bangumi/$id.html"
            },
            source = null.toString()
        )
    }

    fun getPlayLineById(source: String,id: String): List<PlayLine> {
        val cartoonDoc = getCartoonPageDocById(source,id)
        val playLines = arrayListOf<PlayLine>()
        val episodes = arrayListOf<Episode>()
        val playLabel = cartoonDoc.selectXpath("/html/body/div[5]/div[2]/div[1]/div/a")
            .text().trim()
        cartoonDoc.getElementsByClass("box border").forEachIndexed { index, it ->
            val episodeElement = it.getElementsByTag("a")
            val episodeId = if (source == "gugu") {
                val regex = Regex("nid/(\\d+)")
                regex.find(episodeElement.attr("href"))!!.groupValues[1]
            } else {
                Regex("/watch/(\\d+)/(\\d+)/(\\d+).html").find(episodeElement.attr("href"))!!.groupValues[3]
            }
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
        return Jsoup.connect("$guguUrl/index.php/vod/play/id/$id/sid/$sid/nid/$nid.html").userAgent(userAgent).get().html()
    }


    fun getResultPageSize(source: String,keyword: String): Int {

        val text = if (source == "gugu") {
            Jsoup.connect("$guguUrl/index.php/vod/search.html?wd=$keyword").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
        } else {
            Jsoup.connect("$cycUrl/search.html?wd=$keyword").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
        }
        if (text == "") {
            return 1
        }
        val regex = Regex("共(\\d+)条")
        val resultCount = regex.find(text)!!.groupValues[1].toInt()
        return if (resultCount % 10 == 0) {
            resultCount / 10
        } else {
            resultCount / 10 + 1
        }
    }

    fun getSearchResult(source: String,keyword: String, page: Int): Elements {
        return when (source) {
            "gugu" -> {
                Jsoup.connect("$guguUrl/index.php/vod/search/page/$page/wd/$keyword.html").userAgent(userAgent).get()
                    .getElementsByClass("public-list-exp")
            }

            "cyc" -> {
                Jsoup.connect("$cycUrl/search/wd/$keyword/page/$page.html").userAgent(userAgent).get()
                    .getElementsByClass("public-list-exp")
            }

            else -> {
                throw RuntimeException("未知的搜索源")}
        }
    }

    fun weeklyCartoonGroup(site: String,source: Source): List<SourcePage.SingleCartoonPage> {
        val pages = arrayListOf<SourcePage.SingleCartoonPage>()
        val weekElement = when (site) {
            "gugu" -> Jsoup.connect(guguUrl).userAgent(userAgent).get()
            "cyc" -> Jsoup.connect(cycUrl).userAgent(userAgent).get()
            else -> throw RuntimeException("未知的搜索源")
        }
        val weekTabs = weekElement.getElementsByClass("week-select flex box radius overflow rel").first()!!.getElementsByTag("a")
            .map { it.text().trim() }
        weekTabs.forEachIndexed { index, element ->
            val weekModel = weekElement.getElementById("week-module-${index + 1}")
            val weekCartoons = arrayListOf<CartoonCover>()
            val weekModelElement = weekModel!!.getElementsByClass("public-list-div public-list-bj")
            weekModelElement.forEach {
                weekCartoons.add(CartoonCoverImpl(
                    id = getCartoonId(site,it.getElementsByTag("a").attr("href")),
                    title = it.getElementsByTag("a").attr("title"),
                    url = when(site){
                        "gugu" -> "$guguUrl${it.getElementsByTag("a").attr("href")}"
                        "cyc" -> "$cycUrl${it.getElementsByTag("a").attr("href")}"
                        else -> throw RuntimeException("未知的搜索源")
                    },
                    coverUrl = it.getElementsByTag("img").attr("data-src"),
                    source = source.key
                ))
            }
            val page = SourcePage.SingleCartoonPage.WithCover(element, { 0 }){
                withResult(Dispatchers.IO){
                    Pair(null, weekCartoons)
                }
            }
            pages.add(page)
        }
        return pages

    }

    fun getRecomElement(): Elements{
        return Jsoup.connect(cycUrl).userAgent(userAgent).get().getElementsByClass("swiper-wrapper diy-center")[0].getElementsByClass("public-list-exp")
    }

    fun getCartoonId(source: String, url: String): String {
        val regex: Regex = if (source == "gugu") {
            Regex("id/(\\d+)")
        } else {
            Regex("/(\\d+)")
        }
        return regex.find(url)!!.groupValues[1]
    }

    fun getPageAnimeElement(kind: String): Elements {
        if (kind == "tv") {
            return Jsoup.connect(cycUrl).userAgent(userAgent).get().getElementsByClass("flex wrap border-box public-r hide-b-16 diy-center")[0].getElementsByClass("public-list-exp")
        }
        return Jsoup.connect(cycUrl).userAgent(userAgent).get().getElementsByClass("flex wrap border-box public-r hide-b-16 diy-center")[1].getElementsByClass("public-list-exp")
    }

    fun extractVideoUrl(html: String): String {
        val startIndex = html.indexOf("player_aaaa")
        val startBracketIndex = html.indexOf('{', startIndex + 1)
        var endIndex = -1
        var bracketCount = 0
        for (i in (startBracketIndex + 1)..<html.length) {
            val char = html[i]
            if (char == '{') {
                bracketCount++
            } else if (char == '}') {
                if (bracketCount == 0) {
                    endIndex = i
                    break
                }
                bracketCount--
            }
        }
        if (endIndex <= startIndex) {
            throw RuntimeException("未找到播放信息")
        }
        return Gson().fromJson<Map<String, String>>(
            html.substring(startBracketIndex, endIndex + 1),
            Map::class.java
        )["url"] ?: throw RuntimeException("未获取到播放信息")
    }

    fun createCartoonCover(site: String,source: Source,element: Element): CartoonCover {
        return CartoonCoverImpl(
            id = getCartoonId(site, element.attr("href")),
            title = element.let {
                it.attr("title").ifEmpty {
                    it.getElementsByTag("img").attr("alt")
                }
            },
            url = when (site) {
                "gugu" -> "$guguUrl${element.attr("href")}"
                "cyc" -> "$cycUrl${element.attr("href")}"
                else -> throw RuntimeException("未知的搜索源")
            },
            coverUrl = element.getElementsByTag("img").attr("data-src"),
            source = source.key
        )
    }

    fun getExcellentElement(): Elements {
        val pageText = Jsoup.connect("$guguUrl/index.php/label/rb/page/1.html").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
        val regex = Regex("/(\\d+)")
        val pageCount = regex.find(pageText)!!.groupValues[1].toInt()
        val excellentElements = arrayListOf<Element>()
        for (i in 1..pageCount) {
            excellentElements.addAll(Jsoup.connect("$guguUrl/index.php/label/rb/page/$i.html").userAgent(userAgent).get().getElementsByClass("public-list-exp"))
        }
        return Elements(excellentElements)
    }

    fun getNewestElement(site: String, id: Int): Elements {
        val url = when (site) {
            "gugu" -> "$guguUrl/index.php/vod/show/id/$id.html"
            "cyc" -> "$cycUrl/show/$id.html"
            else -> throw RuntimeException("未知的搜索源")
        }
        try {
            return runBlocking {
                val regex = Regex(""".*etector-exec.js.*""").toString()
                val content = webViewHelperV2.renderedHtml(WebViewHelperV2.RenderedStrategy(url, regex)).content
                Log.d("Cartoon", content)
                val elements =
                    Jsoup.parse(content).getElementById("dataList")!!.getElementsByClass("public-list-exp")
                return@runBlocking elements
            }

        }catch (e: Exception){
            stringHelper.toast("获取最新页面数据失败")
            throw RuntimeException("获取最新失败")
        }
    }
}
