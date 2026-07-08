package com.shopverse.cmp.core.architecture.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Builds a NavType that (de)serializes a `@Serializable` payload as a JSON string, so complex
 * types can travel as type-safe navigation arguments. Ported from ProvinCompose.
 */
fun <T : Any> createNavType(
    kClass: KClass<T>,
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : CustomNavType<T>(type = kClass, isNullableAllowed = isNullableAllowed) {

    override fun get(bundle: SavedState, key: String): T? =
        bundle.read {
            getStringOrNull(key)?.takeIf { it.isNotEmpty() }
                ?.let { json.decodeFromString(serializer(), it) }
        }

    override fun parseValue(value: String): T = json.decodeFromString(serializer(), value)

    override fun serializeAsValue(value: T): String = json.encodeToString(serializer(), value)

    override fun put(bundle: SavedState, key: String, value: T) {
        bundle.write { putString(key, json.encodeToString(serializer(), value)) }
    }

    @OptIn(InternalSerializationApi::class)
    private fun serializer(): KSerializer<T> = type.serializer()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomNavType<*>) return false
        return type == other.type
    }

    override fun hashCode(): Int = type.hashCode()
}

abstract class CustomNavType<T : Any>(
    val type: KClass<T>,
    isNullableAllowed: Boolean = false,
) : NavType<T>(isNullableAllowed)
