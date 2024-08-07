package top.phj233.easybangumi_extension_gugufan.gugufan.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil

class GuguFanPageComponent(private val cartoonUtil: CartoonUtil) : ComponentWrapper(), PageComponent {
    override fun getPages(): List<SourcePage> {
        return listOf(
            SourcePage.SingleCartoonPage.WithCover("最近更新", { 1 }){
                withResult(Dispatchers.IO){
                    recentUpdate()
                }
            },
            SourcePage.Group("番剧周期表", false){
                withResult(Dispatchers.IO){
                    cartoonUtil.weeklyCartoonGroup(source)
                }
            },
            SourcePage.Group("最新", false){
                withResult(Dispatchers.IO){
                    newest()
                }
            },
            SourcePage.SingleCartoonPage.WithCover("优质精选",{ 1 }){
                withResult(Dispatchers.IO){
                    excellent()
                }
            }

        )
    }


    private fun recentUpdate(): Pair<Int?, List<CartoonCover>> {
        return cartoonUtil.getRecentUpdate(source)
    }

    private fun newest(): List<SourcePage.SingleCartoonPage> {
        val tab = arrayListOf("连载新番", "完结动画", "动漫电影", "特摄动画", "动漫PV")
        val tabNum = arrayListOf(6, 7, 21, 23, 28)
        return cartoonUtil.createNewestPage(tab, tabNum, source)
    }

    private fun excellent(): Pair<Int?, List<CartoonCover>> {
        val cartoons = arrayListOf<CartoonCover>()
        val excellentElements = cartoonUtil.getExcellentElement()
        excellentElements.forEach {
            cartoons.add(
                CartoonCoverImpl(
                id = cartoonUtil.getCartoonId(source,it.attr("href")),
                title = it.attr("title"),
                url = cartoonUtil.guguUrl + it.attr("href"),
                coverUrl = it.getElementsByTag("img").attr("data-src") ,
                source = source.key
            )
            )
        }
        return Pair(null, cartoons)
    }
}
