package top.phj233.easybangumi_extension_gugufan.cycanime.component

import android.util.Log
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil

class CycanimePlayComponent(
    private val cartoonUtil: CartoonUtil
) : ComponentWrapper(), PlayComponent {
    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode
    ): SourceResult<PlayerInfo> {
        val cartoonPageUrl = "${cartoonUtil.cycUrl}/watch/${summary.id}/${playLine.id}/${episode.id}.html"
        Log.i("CycanimePlayComponent", "cartoonPageUrl: $cartoonPageUrl")
        return withResult(Dispatchers.IO) {
            cartoonUtil.interceptVideoUrl(cartoonPageUrl)
        }
    }

}
