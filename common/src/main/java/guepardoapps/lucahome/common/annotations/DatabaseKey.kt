package guepardoapps.lucahome.common.annotations

import kotlin.reflect.KClass

annotation class DatabaseKey(val column: String, val type: KClass<*>)