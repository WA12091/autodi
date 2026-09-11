package com.example.data.model

enum class CallDisposition(
    val title: String,
    val isSuccess: Boolean = false,
    val shouldRetry: Boolean = false
) {
    CONNECTED("Connected", isSuccess = true),
    CONVERTED("Converted / Sale", isSuccess = true),
    VOICEMAIL("Left Voicemail", shouldRetry = true),
    BUSY("Busy Signal", shouldRetry = true),
    NO_ANSWER("No Answer", shouldRetry = true),
    CALLBACK("Callback Requested", shouldRetry = true),
    WRONG_NUMBER("Wrong / Invalid", isSuccess = false),
    SKIPPED("Skipped", isSuccess = false);

    companion object {
        fun fromString(value: String): CallDisposition {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CONNECTED
        }
    }
}
