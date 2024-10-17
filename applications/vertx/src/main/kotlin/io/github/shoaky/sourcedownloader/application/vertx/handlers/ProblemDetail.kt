package io.github.shoaky.sourcedownloader.application.vertx.handlers

data class ProblemDetail(
    var type: String? = null,
    var title: String,
    var status: Int,
    var detail: String? = null,
    var instance: String? = null
) {

    companion object {

        fun of(status: Int, title: String, detail: String? = null): ProblemDetail {
            return ProblemDetail(
                type = "about:blank",
                title = title,
                status = status,
                detail = detail
            )
        }

        fun notFound(detail: String? = "Resource not found"): ProblemDetail {
            return of(404, "Not Found", detail)
        }

        fun badRequest(detail: String? = "Bad request"): ProblemDetail {
            return of(400, "Bad Request", detail)
        }

        fun unauthorized(detail: String? = "Unauthorized"): ProblemDetail {
            return of(401, "Unauthorized", detail)
        }

        fun internalServerError(detail: String? = "An unexpected error occurred"): ProblemDetail {
            return of(500, "Internal Server Error", detail)
        }

        fun withInstance(status: Int, title: String, instance: String, detail: String? = null): ProblemDetail {
            return ProblemDetail(
                type = "about:blank",
                title = title,
                status = status,
                detail = detail,
                instance = instance
            )
        }
    }
}