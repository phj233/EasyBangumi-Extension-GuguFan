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
            }
        )
    }

    private fun recentUpdate(): Pair<Int?, List<CartoonCover>> {
        val cartoonElements = guguFanUtil.getRecentUpdate()
        val cartoons = arrayListOf<CartoonCover>()
        cartoonElements.forEach {
            cartoons.add(CartoonCoverImpl(
                id = getCartoonId(it.getElementsByClass("public-list-exp").attr("href")),
                title = it.getElementsByClass("public-list-exp").attr("title"),
                url = guguFanUtil.url + it.getElementsByClass("public-list-exp").attr("href"),
                coverUrl = it.getElementsByTag("img").attr("data-src") ,
                source = source.key
            ))
        }
        return Pair(null, cartoons)
    }

    private fun getCartoonId(url: String): String {
        val regex = Regex("id/(\\d+)")
        return regex.find(url)!!.groupValues[1]
    }
}
