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
                val cartoon = guguFanUtil.getCartoonDetailById(getCartoonId(it.attr("href")))
                covers.add(CartoonCoverImpl(
                    id = cartoon.id,
                    title = cartoon.title,
                    url = guguFanUtil.url + it.attr("href"),
                    coverUrl = cartoon.coverUrl,
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
