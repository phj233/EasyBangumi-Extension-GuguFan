package top.phj233.easybangumi_extension_gugufan.cycanime.component

import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil

class CycanimeSearchComponent(private val cartoonUtil: CartoonUtil): ComponentWrapper(), SearchComponent {
    override fun getFirstSearchKey(keyword: String): Int {
        return 1
    }

    override suspend fun search(pageKey: Int, keyword: String): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return withResult(Dispatchers.IO) {
            cartoonUtil.createSearchPage(source,keyword, pageKey)
        }
    }
}
