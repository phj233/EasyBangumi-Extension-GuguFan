package top.phj233.easybangumi_extension_gugufan

import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.SourceFactory

class GuguFanFactory: SourceFactory {
    override fun create(): List<Source> {
        return listOf(GuguFanSource())
    }
}
