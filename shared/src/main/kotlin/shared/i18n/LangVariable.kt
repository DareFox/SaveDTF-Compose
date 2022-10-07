package shared.i18n

import shared.i18n.langs.LanguageResource
import shared.i18n.langs.ProxyLanguageResource
import shared.i18n.langs.en_USLanguageResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private var _LangState: MutableStateFlow<LanguageResource> = MutableStateFlow(ProxyLanguageResource(en_USLanguageResource, en_USLanguageResource))
val LangState: StateFlow<LanguageResource> = _LangState
val Lang
    get() = LangState.value

fun changeLanguage(lang: LanguageResource) {
    _LangState.value = lang
}