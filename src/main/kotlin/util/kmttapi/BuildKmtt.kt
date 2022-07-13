package util.kmttapi

import kmtt.models.enums.Website
import kmtt.impl.*
import ui.viewmodel.SettingsViewModel
import ui.viewmodel.SettingsViewModel.getToken

/**
 * Creating kmttAPI client based on settings.
 * If token specified = build [IAuthKmtt] client
 * Else = build [IPublicKmtt] client
 */
fun betterPublicKmtt(website: Website): IPublicKmtt {
    val token = SettingsViewModel.tokens.getToken(website)

    return if (token.isNotEmpty() && token.isNotBlank()) {
        authKmtt(website, token)
    } else {
        publicKmtt(website)
    }
}