package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

@ControllerAdvice
private class ControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ComponentException::class)
    fun handleComponentException(e: ComponentException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "组件错误"
        problemDetail.detail = e.message
        problemDetail.type = URI.create("component:${e.type}")
        return problemDetail
    }

    @ExceptionHandler(NotFoundException::class)
    fun notFoundException(e: NotFoundException): ProblemDetail {
        return ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(e: IllegalArgumentException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "参数错误"
        problemDetail.detail = e.message
        return problemDetail
    }
}