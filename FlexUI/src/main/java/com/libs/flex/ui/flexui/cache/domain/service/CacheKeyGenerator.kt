package com.libs.flex.ui.flexui.cache.domain.service

import java.security.MessageDigest

/**
 * Service responsible for generating cache keys from JSON strings.
 * Uses MD5 hashing to create unique, consistent keys for caching.
 *
 * MD5 is chosen for its speed and sufficient uniqueness for cache keys.
 * Collision probability is negligible for this use case.
 */
class CacheKeyGenerator {
    /**
     * Generates an MD5 hash key from a JSON string.
     *
     * @param jsonString The JSON string to hash
     * @return A 32-character hexadecimal MD5 hash
     *
     * Example:
     * ```
     * val key = generateKey("""{"type": "componentButton"}""")
     * // Returns: "a1b2c3d4e5f6..."
     * ```
     */
    fun generateKey(jsonString: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(jsonString.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
