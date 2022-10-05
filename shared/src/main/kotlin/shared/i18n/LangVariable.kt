package shared.i18n

import i18n.langs.LanguageResource
import i18n.langs.ProxyLanguageResource
import i18n.langs.en_USLanguageResource
import kotlinx.coroutines.flow.MutableStateFlow

private var _LangState: MutableStateFlow<LanguageResource> = MutableStateFlow(ProxyLanguageResource(en_USLanguageResource, en_USLanguageResource))
val LangState = _LangState
val Lang
    get() = LangState.value

fun changeLanguage(lang: LanguageResource) {
    _LangState.value = lang
}