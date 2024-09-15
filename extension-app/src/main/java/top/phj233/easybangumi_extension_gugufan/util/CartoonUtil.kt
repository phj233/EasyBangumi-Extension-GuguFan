package top.phj233.easybangumi_extension_gugufan.util

import android.util.Log
import com.google.gson.Gson
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.*
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.stream.Collectors

class CartoonUtil(private val webViewHelperV2: WebViewHelperV2, private val stringHelper: StringHelper, private val preferenceHelper: PreferenceHelper) {
    var guguUrl: String = preferenceHelper.get("baseUrl","https://www.gugu3.com")
    var cycUrl: String = "https://www.cycanime.com"
    var nyaUrl: String = preferenceHelper.get("baseUrl","https://www.nyacg.net")
    var userAgent: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0"

    /**
     * 获取最近更新
     * @param source
     * @return Elements 最近更新
     * @see Elements
     */
    fun getRecentUpdate(source: Source): Pair<Int?, List<CartoonCover>> {
        Log.i("CartoonUtil", "获取最近更新 source: ${source.key.split("-")[1]}")
        val recentUpdateElement = when (source.key.split("-")[1]) {
                "gugufan" -> {
                    Jsoup.connect("$guguUrl/index.php/map/index.html").userAgent(userAgent).get()
                        .getElementsByClass("public-list-box public-pic-b [swiper]")
                }
                "cycanime" -> {
                    Jsoup.connect("$cycUrl/map.html").userAgent(userAgent)
                        .get().getElementsByClass("public-list-box public-pic-b")
                }
                "nyafun" -> {
                    Jsoup.connect("$nyaUrl/map.html").userAgent(userAgent)
                        .get().getElementsByClass("public-list-box public-pic-b [swiper]")
                }
                else -> throw RuntimeException("未知的搜索源")
        }
        if (recentUpdateElement.size == 0) {
            Log.e("CartoonUtil", "获取最近更新失败")
        }
        val cartoons = arrayListOf<CartoonCover>()
        recentUpdateElement.forEach {
            cartoons.add(
                CartoonCoverImpl(
                    id = getCartoonId(source,it.getElementsByClass("public-list-exp").attr("href")),
                    title = it.getElementsByClass("public-list-exp").attr("title"),
                    url = when (source.key.split("-")[1]) {
                        "gugufan" -> {
                            "$guguUrl${it.getElementsByClass("public-list-exp").attr("href")}"
                        }
                        "cycanime" -> {
                            "$cycUrl${it.getElementsByClass("public-list-exp").attr("href")}"
                        }
                        "nyafun" -> {
                            "$nyaUrl${it.getElementsByClass("public-list-exp").attr("href")}"
                        }
                        else -> throw RuntimeException("未知的搜索源")
                    },
                    coverUrl = it.getElementsByTag("img").attr("data-src").ifEmpty { it.getElementsByTag("div").attr("data-original") } ,
                    source = source.key
                )
            )
        }
        return Pair(null, cartoons)
    }

    /**
     * 获取番剧详情页
     * @param id 番剧id
     * @param source 番剧源
     * @return Document 番剧详情页Doc
     * @see Document
     */
    private fun getCartoonPageDocById(source: Source,id: String): Document {
        return when (source.key.split("-")[1]) {
            "gugufan" -> Jsoup.connect("$guguUrl/index.php/vod/detail/id/$id.html").userAgent(userAgent).get()
            "cycanime" -> Jsoup.connect("$cycUrl/bangumi/$id.html").userAgent(userAgent).get()
            "nyafun" -> Jsoup.connect("$nyaUrl/bangumi/$id.html").userAgent(userAgent).get()
            else -> throw RuntimeException("未知的搜索源")
        }
    }

    /**
     * 获取番剧详情
     * @param id 番剧id
     * @param source 番剧源
     * @return CartoonImpl 番剧详情
     * @see CartoonImpl
     */
    fun getCartoonDetailById(source: Source,id: String): CartoonImpl {
        val videoDocument = getCartoonPageDocById(source,id)
        val title = videoDocument.getElementsByClass("slide-info-title hide").text()
        val coverUrl = videoDocument.getElementsByClass("detail-pic")[0].getElementsByTag("img").attr("data-src")
        val intro = videoDocument.getElementsByClass("text cor3")[0].text()
        val tag = when (source.key.split("-")[1]) {
            "gugufan" -> {
                videoDocument.getElementsByClass("slide-info hide").last()!!.getElementsByTag("a")
                    .joinToString(",") { it.text().trim() }
            }
            "cycanime" -> {
                videoDocument.getElementsByClass("slide-info hide").last()!!.getElementsByTag("a")
                    .joinToString(",") { it.text().trim() }
            }
            "nyafun" -> {
                videoDocument.getElementsByClass("slide-info hide").first()!!
                    .getElementsByTag("a")
                    .joinToString(",") { it.text().trim() }
            }
            else -> throw RuntimeException("未知的搜索源")
        }

        return CartoonImpl(
            id = id,
            title = title,
            coverUrl = coverUrl,
            description = intro,
            genre = tag,
            url = when (source.key.split("-")[1]) {
                "gugufan" -> {
                    "$guguUrl/index.php/vod/detail/id/$id.html"
                }
                "cycanime" -> {
                    "$cycUrl/bangumi/$id.html"
                }
                "nyafun" -> {
                    "$nyaUrl/bangumi/$id.html"
                }
                else -> throw RuntimeException("未知的搜索源")
            },
            source = source.key
        )
    }

    /**
     * 获取番剧播放线路
     * @param id 番剧id
     * @param source 番剧源
     * @return List<PlayLine> 番剧播放线路
     * @see PlayLine
     */
    fun getPlayLineById(source: Source,id: String): List<PlayLine> {
        val cartoonDoc = getCartoonPageDocById(source,id)
        val playLines = arrayListOf<PlayLine>()
        val episodes = arrayListOf<Episode>()
        val playLabel = cartoonDoc.selectXpath("/html/body/div[5]/div[2]/div[1]/div/a")
            .text().trim()
        cartoonDoc.getElementsByClass("box border").forEachIndexed { index, it ->
            val episodeElement = it.getElementsByTag("a")
            val episodeId = when (source.key.split("-")[1]) {
                    "gugufan" -> {
                        val regex = Regex("nid/(\\d+)")
                        regex.find(episodeElement.attr("href"))!!.groupValues[1]
                    }
                    "cycanime" -> {
                        Regex("/watch/(\\d+)/(\\d+)/(\\d+).html").find(episodeElement.attr("href"))!!.groupValues[3]
                    }
                    "nyafun" -> {
                        Regex("/play/(\\d+)-(\\d+)-(\\d+).html").find(episodeElement.attr("href"))!!.groupValues[3]
                    }
                    else -> throw RuntimeException("未知的搜索源")
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


    /**
     * 获取搜索结果页数
     * @param source 搜索源
     * @param keyword 搜索关键词
     * @return Int 搜索结果页数
     */
    private fun getResultPageSize(source: Source,keyword: String): Int {
        val text = when (source.key.split("-")[1]) {
                "gugufan" -> {
                    Jsoup.connect("$guguUrl/index.php/vod/search.html?wd=$keyword").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
                }
                "cycanime" -> {
                    Jsoup.connect("$cycUrl/search.html?wd=$keyword").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
                }
                "nyafun" -> {
                    Jsoup.connect("$nyaUrl/search.html?wd=$keyword").userAgent(userAgent).get().getElementsByClass("page-tip cor5").text()
                }
                else -> throw RuntimeException("未知的搜索源")
            }
        if (text == "") {
            return 1
        }
        // 共104条数据,当前1/11页
        val regex = Regex("\\d*/(\\d+)页")
        val resultCount = regex.find(text)!!.groupValues[1].toInt()
        return resultCount
    }

    /**
     * 获取搜索结果页
     * @param source 搜索源
     * @param keyword 搜索关键词
     * @param page 页码
     * @return Elements 搜索结果
     * @see Elements
     */
    private fun getSearchResult(source: Source,keyword: String, page: Int): Elements {
        return when (source.key.split("-")[1]) {
            "gugufan" -> {
                Jsoup.connect("$guguUrl/index.php/vod/search/page/$page/wd/$keyword.html").userAgent(userAgent).get()
                    .getElementsByClass("public-list-exp")
            }
            "cycanime" -> {
                Jsoup.connect("$cycUrl/search/wd/$keyword/page/$page.html").userAgent(userAgent).get()
                    .getElementsByClass("public-list-exp")
            }
            "nyafun" -> {
                Jsoup.connect("$nyaUrl/search/wd/$keyword/page/$page.html").userAgent(userAgent).get()
                    .getElementsByClass("public-list-exp")
            }
            else -> {
                throw RuntimeException("未知的搜索源")}
        }
    }

    /**
     * 获取每周番剧
     * @param source 搜索源
     * @return List<SourcePage.SingleCartoonPage> 每周番剧组件
     * @see SourcePage.SingleCartoonPage
     */
    fun weeklyCartoonGroup(source: Source): List<SourcePage.SingleCartoonPage> {
        val pages = arrayListOf<SourcePage.SingleCartoonPage>()
        val weekElement = when (source.key.split("-")[1]) {
            "gugufan" -> Jsoup.connect(guguUrl).userAgent(userAgent).get()
            "cycanime" -> Jsoup.connect(cycUrl).userAgent(userAgent).get()
            "nyafun" -> Jsoup.connect(nyaUrl).userAgent(userAgent).get()
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
                    id = getCartoonId(source,it.getElementsByTag("a").attr("href")),
                    title = it.getElementsByTag("a").attr("title"),
                    url = when(source.key.split("-")[1]){
                        "gugufan" -> "$guguUrl${it.getElementsByTag("a").attr("href")}"
                        "cycanime" -> "$cycUrl${it.getElementsByTag("a").attr("href")}"
                        "nyafun" -> "$nyaUrl${it.getElementsByTag("a").attr("href")}"
                        else -> throw RuntimeException("未知的搜索源")
                    },
                    coverUrl = it.getElementsByTag("img").attr("data-src"),
                    source = source.key
                ))
            }
            val page = SourcePage.SingleCartoonPage.WithCover(element, { 1 }){
                withResult(Dispatchers.IO){
                    Pair(null, weekCartoons)
                }
            }
            pages.add(page)
        }
        return pages

    }


    /**
     * 获取番剧id
     * @param source 搜索源
     * @param url 番剧url
     * @return String 番剧id
     */
    fun getCartoonId(source: Source, url: String): String {
        val regex: Regex = when (source.key.split("-")[1]) {
                "gugufan" -> Regex("id/(\\d+)")
                "cycanime" -> Regex("/(\\d+)")
                "nyafun" -> Regex("/(\\d+)")
                else -> throw RuntimeException("未知的搜索源")
            }
        return regex.find(url)!!.groupValues[1]
    }

    /**
     * 拦截视频播放地址
     * @param url 视频播放地址
     * @return SourceResult<PlayerInfo> 播放信息
     * @see SourceResult
     * @see PlayerInfo
     */
    suspend fun interceptVideoUrl(url: String): PlayerInfo {
        val callBackRegex = Regex(""".*\?verify=.*""").toString()
        val playUrl = webViewHelperV2.renderedHtml(WebViewHelperV2.RenderedStrategy(url, callBackRegex)).interceptResource
        Log.i("CartoonUtil", "playUrl: $playUrl")
        return PlayerInfo(
            uri = playUrl,
            decodeType = if (playUrl.contains("m3u8")){
                PlayerInfo.DECODE_TYPE_HLS
            } else {
                PlayerInfo.DECODE_TYPE_OTHER
            }
        )
    }

    private fun createCartoonCover(source: Source, element: Element): CartoonCover {
        return CartoonCoverImpl(
            id = getCartoonId(source, element.attr("href")),
            title = element.let {
                it.attr("title").ifEmpty {
                    it.getElementsByTag("img").attr("alt")
                }
            },
            url = when (source.key.split("-")[1]) {
                "gugufan" -> "$guguUrl${element.attr("href")}"
                "cycanime" -> "$cycUrl${element.attr("href")}"
                "nyafun" -> "$nyaUrl${element.attr("href")}"
                else -> throw RuntimeException("未知的搜索源")
            },
            coverUrl = element.getElementsByTag("img").attr("data-src"),
            source = source.key
        )
    }

    /**
     * 创建搜索结果页面数据
     * @param source 搜索源
     * @param keyword 关键词
     * @param pageKey 页码
     * @return Pair<Int?, ArrayList<CartoonCover>> 页码和番剧列表
     */
    fun createSearchPage(source: Source, keyword: String, pageKey: Int): Pair<Int?, ArrayList<CartoonCover>> {
        val pageSize = getResultPageSize(source, keyword)
        val cartoonCovers = getSearchResult(source, keyword, pageKey)
        val covers = arrayListOf<CartoonCover>()
        cartoonCovers.forEach {
            covers.add(
                CartoonCoverImpl(
                    id = getCartoonId(source,it.attr("href")),
                    title = it.getElementsByTag("img").attr("alt")
                        .substring(0, it.getElementsByTag("img").attr("alt").length - 3),
                    url = when(source.key.split("-")[1]){
                        "gugufan" -> "$guguUrl${it.attr("href")}"
                        "cycanime" -> "$cycUrl${it.attr("href")}"
                        "nyafun" -> "$nyaUrl${it.attr("href")}"
                        else -> throw RuntimeException("未知的搜索源")
                    },
                    coverUrl = it.getElementsByTag("img").attr("data-src"),
                    source = source.key
                )
            )
        }
        val next = if (pageSize > pageKey) pageKey + 1 else null
        return Pair(next, covers)
    }

    //GuguFan:
    /**
     * 获取GuguFan的播放页面html
     */
    fun getPlayerPageHtml(id: String, sid: String, nid: String): String {
        return Jsoup.connect("$guguUrl/index.php/vod/play/id/$id/sid/$sid/nid/$nid.html").userAgent(userAgent).get().html()
    }

    /**
     * 提取GuguFan的视频播放地址
     * @param html 源html
     * @return String 视频播放地址
     */
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

    /**
     * 获取GuguFan的精选番剧
     * @return Elements 精选番剧元素
     */
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

    //CycAnime and GuguFan:
    /**
     * 获取按最新查询的元素
     * @param source 搜索源
     * @param id 番剧id
     * @return Elements 最新页面
     */
    private fun getNewestElement(source: Source, id: Int): Elements {
        val url = when (source.key.split("-")[1]) {
            "gugufan" -> "$guguUrl/index.php/vod/show/id/$id.html"
            "cycanime" -> "$cycUrl/show/$id.html"
            else -> throw RuntimeException("未知的搜索源")
        }
        try {
            return runBlocking {
                val regex = Regex(""".*etector-exec.js.*""").toString()
                val content = webViewHelperV2.renderedHtml(WebViewHelperV2.RenderedStrategy(url, regex, timeOut = 1400L)).content
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

    /**
     * 创建最新页面组件
     * @param tab 标签
     * @param tabNum 标签对应的id
     * @return List<SourcePage.SingleCartoonPage.WithCover> 标签对应的番剧列表
     * @see getNewestElement
     * @see SourcePage.SingleCartoonPage.WithCover
     */
    fun createNewestPage(tab:ArrayList<String>,tabNum:ArrayList<Int>,source: Source): List<SourcePage.SingleCartoonPage.WithCover> {
        val tabMap = tab.zip(tabNum).associate { (name, num) -> name to num }
        val pages = tabMap.map { (name, num) ->
            val cartoonCover = getNewestElement(source, num).parallelStream().map {
                createCartoonCover(source,it)
            }.collect(Collectors.toList())
            SourcePage.SingleCartoonPage.WithCover(name, { 0 }) {
                withResult(Dispatchers.IO) {
                    Pair(null, cartoonCover)
                }
            }
        }
        return pages
    }

    //CycAnime and NyaFan:

    private fun getPageAnimeElement(kind: String, source: Source): Elements {
        return when(kind){
        "tv" -> when(source.key.split("-")[1]){
            "cycanime" -> Jsoup.connect(cycUrl).userAgent(userAgent).get().getElementsByClass("flex wrap border-box public-r hide-b-16 diy-center")[0].getElementsByClass("public-list-exp")
            "nyafun" -> Jsoup.connect(nyaUrl).userAgent(userAgent).get().getElementsByClass("flex wrap border-box public-r hide-b-20")[0].getElementsByClass("public-list-exp")
            else -> throw RuntimeException("未知的搜索源")
        }
        "movie" -> when(source.key.split("-")[1]){
            "cycanime" -> Jsoup.connect(cycUrl).userAgent(userAgent).get().getElementsByClass("flex wrap border-box public-r hide-b-16 diy-center")[1].getElementsByClass("public-list-exp")
            "nyafun" -> Jsoup.connect(nyaUrl).userAgent(userAgent).get().getElementsByClass("flex wrap border-box public-r hide-b-20")[1].getElementsByClass("public-list-exp")
            else -> throw RuntimeException("未知的搜索源")
        }else -> throw RuntimeException("未知的kind")
        }
    }


    /**
     * 获取推荐番剧
     * @return Elements 推荐番剧
     */
    private fun getRecomElement(source: Source): Elements{
        return when(source.key.split("-")[1]){
            "cycanime" -> Jsoup.connect(cycUrl).userAgent(userAgent).get().getElementsByClass("swiper-wrapper diy-center")[0].getElementsByClass("public-list-exp")
            "nyafun" -> Jsoup.connect(nyaUrl).userAgent(userAgent).get().getElementsByClass("swiper-wrapper")[2].getElementsByClass("public-list-exp")
            else -> {throw RuntimeException("未知的搜索源")}
        }
    }

    /**
     * 创建首页组件
     * @param tab 标签
     * @param source 番剧源
     * @return List<SourcePage.SingleCartoonPage> 首页组件
     * @see getRecomElement
     * @see getPageAnimeElement
     * @see SourcePage.SingleCartoonPage
     */
    fun createHomePage(tab: ArrayList<String>,source: Source): List<SourcePage.SingleCartoonPage.WithCover> {
        val recomEl = getRecomElement(source)
        val tvAnimeEl = getPageAnimeElement("tv",source)
        val movieAnimeEl = getPageAnimeElement("movie",source)
        val recomCartoons = recomEl.parallelStream().map { createCartoonCover(source, it) }
            .collect(Collectors.toList())
        val tvAnimeCartoons = tvAnimeEl.parallelStream().map { createCartoonCover(source, it) }
            .collect(Collectors.toList())
        val movieAnimeCartoons = movieAnimeEl.parallelStream().map { createCartoonCover(source, it) }
            .collect(Collectors.toList())
        val pages = tab.mapIndexed { index, title ->
            SourcePage.SingleCartoonPage.WithCover(title, { 0 }) {
                withResult(Dispatchers.IO) {
                    when (index) {
                        0 -> Pair(null, recomCartoons)
                        1 -> Pair(null, tvAnimeCartoons)
                        2 -> Pair(null, movieAnimeCartoons)
                        else -> Pair(null, arrayListOf())
                    }
                }
            }
        }

        return pages
    }
    // NyaFun:

    /**
     * 根据番剧种类以及排序方式获取番剧列表
     * @param category 番剧种类
     * @return List<SourcePage.SingleAsyncPage> 番剧列表
     * @see SourcePage.SingleAsyncPage
     */
    fun createNyaAnimeGroup(source: Source,category: String): ArrayList<SourcePage.SingleCartoonPage> {
        val sort = listOf(
            "time" to "按最新",
            "hits" to "按最热",
            "score" to "按评分"
        )
        val pages = arrayListOf<SourcePage.SingleCartoonPage>()
        sort.forEach { (key, value) ->
            pages.add(SourcePage.SingleCartoonPage.WithCover(value, { 1 }) {
                withResult(Dispatchers.IO) {
                    fetchAnimeOfCategory(source,category,key,it)!!
               }
            })
        }
        return pages



    }
    private fun fetchAnimeOfCategory(source: Source, category: String, sort: String, page: Int): Pair<Int, ArrayList<CartoonCover>>? {
        val url = "$nyaUrl/show/$category/by/$sort/page/$page.html"
        val cartoons: ArrayList<CartoonCover> = arrayListOf()
        Jsoup.connect(url).userAgent(userAgent).get().getElementsByClass("public-list-exp").map {
            cartoons.add(createCartoonCover(source, it))
        }
        return if (cartoons.isEmpty()) null else page + 1 to cartoons
    }
}


