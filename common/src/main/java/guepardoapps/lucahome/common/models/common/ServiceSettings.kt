package guepardoapps.lucahome.common.models.common

data class ServiceSettings(
        val id: Int,
        var reloadEnabled: Boolean,
        var reloadTimeoutMs: Int,
        var notificationEnabled: Boolean)