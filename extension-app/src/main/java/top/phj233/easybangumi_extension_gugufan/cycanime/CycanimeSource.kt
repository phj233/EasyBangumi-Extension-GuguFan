package top.phj233.easybangumi_extension_gugufan.cycanime

import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.ExtensionSource
import top.phj233.easybangumi_extension_gugufan.R
import top.phj233.easybangumi_extension_gugufan.cycanime.component.*
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil
import kotlin.reflect.KClass

class CycanimeSource : ExtensionSource(), ExtensionIconSource {
    override val describe: String
        get() = "纯纯看番-次元城动漫源"
    override val label: String
        get() = "次元城动漫"
    override val version: String
        get() = "1.2"
    override val versionCode: Int
        get() = 11

    override fun getIconResourcesId(): Int {
        return R.drawable.cycanime
    }
    override val sourceKey: String
        get() = "cycanime"

    override fun register(): List<KClass<*>> {
        return listOf(
            CycanimePageComponent::class,
            CycanimeDetailedComponent::class,
            CycanimePlayComponent::class,
            CycanimeSearchComponent::class,
            CycanimeUpdateComponent::class,
            CartoonUtil::class,
            CycanimePreferenceComponent::class,
            WebViewHelperV2::class
        )
    }


}
