package on.insurance.supportbot

import org.springframework.context.MessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: MessageSource) {

    @ExceptionHandler(DemoException::class)
    fun handleDemoException(exception: DemoException) = when (exception) {
        is GeneralApiException -> ResponseEntity.badRequest()
            .body(exception.response(errorMessageSource, exception.text))

        is NullPointerException -> ResponseEntity.badRequest()
            .body(exception.response(errorMessageSource, exception.text))

        else -> ResponseEntity.badRequest().body(BaseMessage(500, "Some exception"))
    }
}