package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public fun interface ValidationRule {
    public fun validate(value: FieldValue?): Either<ValidationError, Unit>
}
