package ui.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LangViewModel {
    private val _currentLanguage: MutableStateFlow<LanguageResource> = MutableStateFlow(ProxyLanguageResource(ru_RULanguageResource, DefaultLanguageResource))
    val currentLanguage: StateFlow<LanguageResource> = _currentLanguage

    fun switchLanguage(language: LanguageResource) {
        _currentLanguage.value = ProxyLanguageResource(language, DefaultLanguageResource)
    }
}