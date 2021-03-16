package no.nav.helse.grensekomp.web.api.dto

import no.nav.helse.grensekomp.web.api.dto.validation.ValidationProblemDetail

data class PostListResponseDto(
    val status: Status,
    val validationErrors: List<ValidationProblemDetail>? = null,
    val genericMessage: String? = null,
    val referenceNumber: String? = null
) {
    public enum class Status {
        OK,
        VALIDATION_ERRORS,
        GENERIC_ERROR
    }
}