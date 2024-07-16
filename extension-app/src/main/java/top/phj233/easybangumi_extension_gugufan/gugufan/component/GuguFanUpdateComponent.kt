package top.phj233.easybangumi_extension_gugufan.gugufan.component

import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.update.UpdateComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import top.phj233.easybangumi_extension_gugufan.util.CartoonUtil

class GuguFanUpdateComponent(private val cartoonUtil: CartoonUtil): ComponentWrapper(), UpdateComponent {
    override suspend fun update(cartoon: Cartoon, oldPlayLine: List<PlayLine>): SourceResult<Cartoon> {
        return withResult(Dispatchers.IO) {
            val newPlayLine = cartoonUtil.getPlayLineById("gugu", cartoon.id)
            if (newPlayLine.size > oldPlayLine.size) {
                cartoon.isUpdate = true
            }
            cartoon
        }
    }
}
