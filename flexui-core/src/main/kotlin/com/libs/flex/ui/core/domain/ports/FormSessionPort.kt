package com.libs.flex.ui.core.domain.ports

import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.value.SessionId

public interface FormSessionPort {
    public suspend fun load(id: SessionId): FormSession?
    public suspend fun save(session: FormSession)
}
