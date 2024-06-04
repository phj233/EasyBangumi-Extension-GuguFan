package top.phj233.easybangumi_extension_gugufan.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.GuguFanUtil

class GuguFanPageComponent(private val guguFanUtil: GuguFanUtil) : ComponentWrapper(),PageComponent {
    override fun getPages(): List<SourcePage> {
        return listOf(
            SourcePage.SingleCartoonPage.WithCover("最近更新", { 1 }){
                withResult(Dispatchers.IO){
                    recentUpdate()
                }
            },
            SourcePage.Group("番剧周期表", false){
                withResult(Dispatchers.IO){
                    weeklyCartoon()
                }
            }
        )
    }

    private fun recentUpdate(): Pair<Int?, List<CartoonCover>> {
        val cartoonElements = guguFanUtil.getRecentUpdate()
        val cartoons = arrayListOf<CartoonCover>()
        cartoonElements.forEach {
            val cartoon = guguFanUtil.getCartoonDetailById(getCartoonId(it.getElementsByClass("public-list-exp").attr("href")))
            cartoons.add(CartoonCoverImpl(
                id = cartoon.id,
                title = cartoon.title,
                coverUrl = cartoon.coverUrl,
                intro = cartoon.description,
                url = cartoon.url,
                source = source.key
            ))
        }
        return Pair(null, cartoons)
    }

    private fun weeklyCartoon(): List<SourcePage.SingleCartoonPage> {
        val pages = arrayListOf<SourcePage.SingleCartoonPage>()
        val weekElement = guguFanUtil.getWeekElement()
        val weekTabs = weekElement.getElementsByClass("week-select flex box radius overflow rel").first()!!.getElementsByTag("a")
                .map { it.text().trim() }
        weekTabs.forEachIndexed { index, element ->
            val weekModel = weekElement.getElementById("week-module-${index + 1}")
            val weekCartoons = arrayListOf<CartoonCover>()
            val weekModelElement = weekModel!!.getElementsByClass("public-list-div public-list-bj")
            weekModelElement.forEach {
                val cartoon = guguFanUtil.getCartoonDetailById(getCartoonId(it.getElementsByTag("a").attr("href")))
                weekCartoons.add(CartoonCoverImpl(
                    id = cartoon.id,
                    title = cartoon.title,
                    coverUrl = cartoon.coverUrl,
                    intro = cartoon.description,
                    url = cartoon.url,
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
    private fun getCartoonId(url: String): String {
        val regex = Regex("id/(\\d+)")
        return regex.find(url)!!.groupValues[1]
    }
}
