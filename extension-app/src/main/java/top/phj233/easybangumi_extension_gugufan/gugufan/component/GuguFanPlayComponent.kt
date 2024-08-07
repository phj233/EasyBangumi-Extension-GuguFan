package top.phj233.easybangumi_extension_gugufan.gugufan.component

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

class GuguFanPlayComponent(
    private val cartoonUtil: CartoonUtil
) : ComponentWrapper(), PlayComponent {
    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode
    ): SourceResult<PlayerInfo> {
        return withResult(Dispatchers.IO) {
            val guguFanHtml = cartoonUtil.getPlayerPageHtml(summary.id, playLine.id, episode.id)
            val cartoonUrl = cartoonUtil.extractVideoUrl(guguFanHtml)
            Log.i("GuguFanPlayComponent", "cartoonM3U8Url: $cartoonUrl")
            PlayerInfo(
                uri = cartoonUrl,
                decodeType = if (cartoonUrl.contains("m3u8")) PlayerInfo.DECODE_TYPE_HLS else PlayerInfo.DECODE_TYPE_OTHER
            )
        }

    }
}
