package xyz.shoaky.sourcedownloader.api

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import xyz.shoaky.sourcedownloader.sdk.component.ComponentException
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
}