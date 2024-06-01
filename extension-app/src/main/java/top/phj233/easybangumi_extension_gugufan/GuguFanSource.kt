package top.phj233.easybangumi_extension_gugufan

import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.ExtensionSource
import top.phj233.easybangumi_extension_gugufan.component.GuguFanDetailedComponent
import top.phj233.easybangumi_extension_gugufan.component.GuguFanPageComponent
import kotlin.reflect.KClass

class GuguFanSource : ExtensionSource(), ExtensionIconSource {
    override val describe: String
        get() = "纯纯看番-咕咕番剧源插件"
    override val label: String
        get() = "咕咕番"
    override val version: String
        get() = "1.0"
    override val versionCode: Int
        get() = 6
    override val sourceKey: String
        get() = "gugufan"

    override fun getIconResourcesId(): Int {
        return R.drawable.gugufan
    }

    override fun register(): List<KClass<*>> {
        return listOf(
            GuguFanPageComponent::class,
            GuguFanDetailedComponent::class
        )
    }

}
