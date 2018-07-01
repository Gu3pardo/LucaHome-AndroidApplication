package guepardoapps.lucahome.common.models.common

data class NotificationContent(
        val id: Int,
        val title: String,
        val text: String,
        val iconId: Int,
        val largeIconId: Int,
        val receiver: Class<*>,
        val cancelable: Boolean)