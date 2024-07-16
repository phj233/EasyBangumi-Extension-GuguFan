package top.phj233.easybangumi_extension_gugufan.cycanime.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil
import java.util.stream.Collectors

class CycanimePageComponent(private val cartoonUtil: CartoonUtil) : ComponentWrapper(), PageComponent {
    override fun getPages(): List<SourcePage> {
        return listOf(
            SourcePage.Group("首页", false) {
                withResult(Dispatchers.IO) {
                    homePage()
                }
            },
            SourcePage.Group("周番剧表", false) {
                withResult(Dispatchers.IO) {
                    cartoonUtil.weeklyCartoonGroup("cyc", source)
                }
            },
            SourcePage.Group("最新", false){
                withResult(Dispatchers.IO) {
                    newest()
                }
            }
        )
    }

    private fun homePage(): List<SourcePage.SingleCartoonPage> {
        val pageTab = arrayListOf("推荐", "TV动画", "剧场动画")
        val recomEl = cartoonUtil.getRecomElement()
        val tvAnimeEl = cartoonUtil.getPageAnimeElement("tv")
        val movieAnimeEl = cartoonUtil.getPageAnimeElement("movie")
        val recomCartoons = recomEl.parallelStream().map { cartoonUtil.createCartoonCover("cyc", source, it) }
            .collect(Collectors.toList())
        val tvAnimeCartoons = tvAnimeEl.parallelStream().map { cartoonUtil.createCartoonCover("cyc", source, it) }
            .collect(Collectors.toList())
        val movieAnimeCartoons = movieAnimeEl.parallelStream().map { cartoonUtil.createCartoonCover("cyc", source, it) }
            .collect(Collectors.toList())

        val pages = pageTab.mapIndexed { index, title ->
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

    private suspend fun newest(): List<SourcePage.SingleCartoonPage> {
        val tab = arrayListOf("TV动画", "剧场版", "4K专区")
        val tabNum = arrayListOf(20,21,26)
        val tabMap = tab.zip(tabNum).associate { (name, num) -> name to num }
        val pages = tabMap.map { (name, num) ->
            val cartoonCover = cartoonUtil.getNewestElement("cyc", num).parallelStream().map {
                cartoonUtil.createCartoonCover("cyc", source, it)
            }.collect(Collectors.toList())
            SourcePage.SingleCartoonPage.WithCover(name, { 0 }) {
                withResult(Dispatchers.IO) {
                    Pair(null, cartoonCover)
                }
            }
        }
        return pages
    }
}
