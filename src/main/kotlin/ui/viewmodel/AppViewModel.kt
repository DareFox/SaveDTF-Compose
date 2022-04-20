package ui.viewmodel

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import logic.Version
import logic.ktor.Client
import logic.ktor.rateRequest
import util.toVersionOrNull

object AppViewModel {
    const val VERSION = "1.0.1"
    val currentVersionObject = VERSION.toVersionOrNull()!!

    private val repoName = "DareFox/SaveDTF-compose"
    val latestVersionURL = "https://github.com/$repoName/releases/latest"
    private val latestVersionAPI = "https://api.github.com/repos/$repoName/releases/latest"

    suspend fun getLastVersionOrNull(): Version? {
        return try {
            val json = Client.rateRequest<JsonElement> {
                method = HttpMethod.Get
                url(latestVersionAPI)
            }.jsonObject

            val versionJson = json["tag_name"]?.jsonPrimitive

            if (versionJson?.isString == true) {
                return versionJson.toString().toVersionOrNull()
            } else {
                return null
            }
        } catch (ex: Exception) {
            null
        }
    }
}

