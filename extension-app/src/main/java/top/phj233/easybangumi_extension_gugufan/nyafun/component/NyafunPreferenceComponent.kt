package top.phj233.easybangumi_extension_gugufan.nyafun.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.preference.SourcePreference

class NyafunPreferenceComponent:ComponentWrapper(),PreferenceComponent {
    override fun register(): List<SourcePreference> {
        return listOf(SourcePreference.Edit("Nyafun网址 别带/","baseUrl","https://www.nyacg.net"))
    }
}
