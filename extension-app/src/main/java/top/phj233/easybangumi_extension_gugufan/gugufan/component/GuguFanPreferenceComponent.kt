package top.phj233.easybangumi_extension_gugufan.gugufan.component

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.preference.SourcePreference

class GuguFanPreferenceComponent: ComponentWrapper(), PreferenceComponent {
    override fun register(): List<SourcePreference> {
         return listOf( SourcePreference.Edit("咕咕番动漫网址 别带/","baseUrl","https://www.gugu3.com"))
    }
}
