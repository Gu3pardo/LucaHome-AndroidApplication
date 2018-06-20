package guepardoapps.lucahome.common.annotations

import kotlin.reflect.KClass

annotation class JsonKey(val parent: String, val key: String, val type: KClass<*>)