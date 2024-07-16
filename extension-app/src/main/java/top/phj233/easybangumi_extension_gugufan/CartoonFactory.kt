package top.phj233.easybangumi_extension_gugufan

import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.SourceFactory
import top.phj233.easybangumi_extension_gugufan.cycanime.CycanimeSource
import top.phj233.easybangumi_extension_gugufan.gugufan.GuguFanSource

@Suppress("unused")
class CartoonFactory: SourceFactory {
    override fun create(): List<Source> {
        return listOf(
            GuguFanSource(),
            CycanimeSource()
        )
    }
}
