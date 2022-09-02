package on.insurance.supportbot

import on.insurance.supportbot.teligram.ErrorCode
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder


abstract class DemoException : RuntimeException() {

    abstract fun errorCode(): ErrorCode

    fun response(source: MessageSource, vararg args: Any) =
        BaseMessage(errorCode().code, source.getMessage(errorCode().name, args, LocaleContextHolder.getLocale()))
}

class GeneralApiException(val text: String) : DemoException() {
    override fun errorCode() = ErrorCode.GENERAL
}

class NullPointerException(val text: String) : DemoException() {
    override fun errorCode() = ErrorCode.OBJECT_NOT_FOUND
}