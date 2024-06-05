package top.phj233.easybangumi_extension_gugufan.component

import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.GuguFanUtil

class GuguFanSearchComponent(private val guguFanUtil: GuguFanUtil): ComponentWrapper(), SearchComponent {
    override fun getFirstSearchKey(keyword: String): Int {
        return 1
    }

    override suspend fun search(pageKey: Int, keyword: String): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return withResult(Dispatchers.IO) {
            val pageSize = guguFanUtil.getResultPageSize(keyword)
            val cartoonCovers = guguFanUtil.getSearchResult(keyword, pageKey)
            val covers = arrayListOf<CartoonCover>()
            cartoonCovers.forEach {
                covers.add(CartoonCoverImpl(
                    id = getCartoonId(it.attr("href")),
                    title = it.getElementsByTag("img").attr("alt")
                        .substring(0, it.getElementsByTag("img").attr("alt").length - 3),
                    url = guguFanUtil.url + it.attr("href"),
                    coverUrl = it.getElementsByTag("img").attr("data-src"),
                    source = source.key
                ))
            }
            val next = if (pageSize > pageKey) pageKey + 1 else null
            Pair(next, covers)
        }
    }

    private fun getCartoonId(attr: String): String {
        val regex = Regex("id/(\\d+)")
        return regex.find(attr)!!.groupValues[1]
    }

}
