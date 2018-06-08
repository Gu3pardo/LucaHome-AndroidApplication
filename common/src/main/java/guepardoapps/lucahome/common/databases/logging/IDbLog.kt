package guepardoapps.lucahome.common.databases.logging

import java.io.Serializable
import java.sql.Date

interface IDbLog : Serializable {
    fun getId(): Int

    fun getDateTime(): Date

    fun getSeverity(): Severity

    fun getTag(): String

    fun getDescription(): String
}