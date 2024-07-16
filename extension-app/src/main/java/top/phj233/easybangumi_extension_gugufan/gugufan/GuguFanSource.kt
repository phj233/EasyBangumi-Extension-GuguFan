package top.phj233.easybangumi_extension_gugufan.gugufan

import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.ExtensionSource
import top.phj233.easybangumi_extension_gugufan.R
import top.phj233.easybangumi_extension_gugufan.gugufan.component.*
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil
import kotlin.reflect.KClass

class GuguFanSource : ExtensionSource(), ExtensionIconSource {
    override val describe: String
        get() = "纯纯看番-咕咕番剧源"
    override val label: String
        get() = "咕咕番"
    override val version: String
        get() = "1.1"
    override val versionCode: Int
        get() = 11
    override val sourceKey: String
        get() = "gugufan"

    override fun getIconResourcesId(): Int {
        return R.drawable.gugufan
    }

    override fun register(): List<KClass<*>> {
        return listOf(
            GuguFanPageComponent::class,
            GuguFanDetailedComponent::class,
            GuguFanPlayComponent::class,
            GuguFanSearchComponent::class,
            GuguFanUpdateComponent::class,
            CartoonUtil::class,
            WebViewHelperV2::class
        )
    }

}
