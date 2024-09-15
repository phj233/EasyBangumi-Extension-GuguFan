package top.phj233.easybangumi_extension_gugufan.nyafun

import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.ExtensionSource
import top.phj233.easybangumi_extension_gugufan.R
import top.phj233.easybangumi_extension_gugufan.nyafun.component.*
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil
import kotlin.reflect.KClass

class NyafunSource: ExtensionSource(), ExtensionIconSource {
    override val describe: String
        get() = "纯纯看番-Nyafun动漫源"
    override val label: String
        get() = "Nyafun"
    override val version: String
        get() = "1.2"
    override val versionCode: Int
        get() = 11

    override fun getIconResourcesId(): Int {
        return R.drawable.nyafun
    }

    override val sourceKey: String
        get() = "nyafun"
    override fun register(): List<KClass<*>> {
        return listOf(
            NyafunPageComponent::class,
            NyafunDetailedComponent::class,
            NyafunPlayComponent::class,
            NyafunSearchComponent::class,
            NyafunUpdateComponent::class,
            CartoonUtil::class,
            NyafunPreferenceComponent::class,
            WebViewHelperV2::class
        )
    }
}
