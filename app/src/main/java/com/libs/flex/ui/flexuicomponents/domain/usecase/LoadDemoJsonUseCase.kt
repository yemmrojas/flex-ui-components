package com.libs.flex.ui.flexuicomponents.domain.usecase

import com.libs.flex.ui.flexuicomponents.domain.model.DemoType
import com.libs.flex.ui.flexuicomponents.domain.repository.JsonAssetRepository
import javax.inject.Inject

/**
 * Use case for loading demo JSON content.
 * Encapsulates the business logic for loading JSON assets.
 */
class LoadDemoJsonUseCase @Inject constructor(
    private val repository: JsonAssetRepository
) {
    /**
     * Loads JSON content for a specific demo type.
     *
     * @param demoType The type of demo to load
     * @return JSON string content or null if loading fails
     */
    suspend operator fun invoke(demoType: DemoType): String? {
        return repository.loadJsonFromAssets(demoType.jsonFileName)
    }
}
