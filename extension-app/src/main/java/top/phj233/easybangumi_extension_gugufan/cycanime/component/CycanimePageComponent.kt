package top.phj233.easybangumi_extension_gugufan.cycanime.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil

class CycanimePageComponent(private val cartoonUtil: CartoonUtil) : ComponentWrapper(), PageComponent {
    override fun getPages(): List<SourcePage> {
        return listOf(
            SourcePage.Group("首页", false) {
                withResult(Dispatchers.IO) {
                    homePage()
                }
            },
            SourcePage.SingleCartoonPage.WithCover("最近更新", { 1 }){
                withResult(Dispatchers.IO){
                    cartoonUtil.getRecentUpdate(source)
                }
            },
            SourcePage.Group("周番剧表", false) {
                withResult(Dispatchers.IO) {
                    cartoonUtil.weeklyCartoonGroup(source)
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
        return cartoonUtil.createHomePage(pageTab, source)
    }

    private fun newest(): List<SourcePage.SingleCartoonPage> {
        val tab = arrayListOf("TV动画", "剧场版", "4K专区")
        val tabNum = arrayListOf(20,21,26)
        return cartoonUtil.createNewestPage(tab, tabNum, source)
    }
}
