package top.phj233.easybangumi_extension_gugufan.nyafun.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil

class NyafunPageComponent(private val cartoonUtil: CartoonUtil) : ComponentWrapper(), PageComponent {
    override fun getPages(): List<SourcePage> {
        return listOf(
            SourcePage.Group("首页", false) {
                withResult(Dispatchers.IO) {
                    val tab = arrayListOf("本月热门", "番剧", "剧场")
                    cartoonUtil.createHomePage(tab, source)
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
            }
        )
    }

}
