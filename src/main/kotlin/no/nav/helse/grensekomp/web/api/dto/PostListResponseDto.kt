package no.nav.helse.grensekomp.web.api.dto

import no.nav.helse.grensekomp.web.api.dto.validation.ValidationProblemDetail

data class PostListResponseDto(
    var status: Status,
    val validationErrors: List<ValidationProblemDetail>? = emptyList(),
    val genericMessage: String? = null,
    val referenceNumber: String? = null
) {
    public enum class Status {
        OK,
        VALIDATION_ERRORS,
        GENERIC_ERROR
    }
}