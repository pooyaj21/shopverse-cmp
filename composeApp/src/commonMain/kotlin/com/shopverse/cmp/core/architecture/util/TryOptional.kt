package com.shopverse.cmp.core.architecture.util

/** Runs [block], swallowing any exception and returning null instead. */
inline fun <T> tryOptional(block: () -> T): T? = try {
    block()
} catch (_: Throwable) {
    null
}
