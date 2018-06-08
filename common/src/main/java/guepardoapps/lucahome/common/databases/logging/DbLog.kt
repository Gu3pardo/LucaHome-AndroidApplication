package guepardoapps.lucahome.common.databases.logging

import java.sql.Date

class DbLog(private val id: Int,
            private val dateTime: Date,
            private val severity: Severity,
            private val tag: String,
            private val description: String) : IDbLog {

    override fun getId(): Int {
        return id
    }

    override fun getDateTime(): Date {
        return dateTime
    }

    override fun getSeverity(): Severity {
        return severity
    }

    override fun getTag(): String {
        return tag
    }

    override fun getDescription(): String {
        return description
    }
}