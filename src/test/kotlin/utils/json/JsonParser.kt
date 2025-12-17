package utils.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    explicitNulls = true
    encodeDefaults = true
}