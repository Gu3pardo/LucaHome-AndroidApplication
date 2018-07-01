package guepardoapps.lucahome.common.databases.logging

import java.sql.Date

data class DbLog(
        val id: Int,
        val dateTime: Date,
        val severity: Severity,
        val tag: String,
        val description: String)