package top.phj233.easybangumi_extension_gugufan.component

import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.GuguFanUtil

class GuguFanDetailedComponent : ComponentWrapper(), DetailedComponent {
    override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
        return withResult(Dispatchers.IO) {
            val cartoon = getCartoonDetailById(summary.id)
            val playLine = getPlayLineById(summary.id)
            Pair(cartoon, playLine)
        }
    }

    override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> {
        return withResult(Dispatchers.IO) {
            getCartoonDetailById(summary.id)
        }
    }

    override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> {
        return withResult(Dispatchers.IO) {
            getPlayLineById(summary.id)
        }
    }

    private fun getCartoonDetailById(id: String): Cartoon {
        val videoDetail = GuguFanUtil().getCartoonDetailById(id)
        videoDetail.source = source.key
        return videoDetail
    }

    private fun getPlayLineById(string: String): List<PlayLine> {
        return GuguFanUtil().getPlayLineById(string)
    }

}
