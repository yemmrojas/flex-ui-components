package com.libs.flex.ui.flexuicomponents.data.repository

import android.content.Context
import android.util.Log
import com.libs.flex.ui.flexuicomponents.domain.repository.JsonAssetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * Implementation of JsonAssetRepository.
 * Handles loading JSON files from Android assets folder.
 */
class JsonAssetRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : JsonAssetRepository {

    override suspend fun loadJsonFromAssets(fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading JSON from assets: $fileName", e)
            null
        }
    }

    companion object {
        private const val TAG = "JsonAssetRepository"
    }
}
