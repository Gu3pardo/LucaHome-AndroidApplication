package guepardoapps.lucahome.common.models.common

class ServiceSettings(
        val id: Int,
        var reloadEnabled: Boolean,
        var reloadTimeoutMs: Int,
        var notificationEnabled: Boolean)