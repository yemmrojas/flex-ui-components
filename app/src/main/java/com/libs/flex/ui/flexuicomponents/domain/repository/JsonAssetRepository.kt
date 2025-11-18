package com.libs.flex.ui.flexuicomponents.domain.repository

/**
 * Repository interface for loading JSON assets.
 * Follows the Repository pattern from Clean Architecture.
 */
interface JsonAssetRepository {
    /**
     * Loads JSON content from assets folder.
     *
     * @param fileName Name of the JSON file in assets
     * @return JSON string content or null if file not found
     */
    suspend fun loadJsonFromAssets(fileName: String): String?
}
