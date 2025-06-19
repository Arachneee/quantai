package com.quantai.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun Logger.traceLog(crossinline messageBlock: () -> Any?) {
    if (isTraceEnabled) trace(messageBlock().toString())
}

inline fun Logger.debugLog(crossinline messageBlock: () -> Any?) {
    if (isDebugEnabled) debug(messageBlock().toString())
}

inline fun Logger.infoLog(crossinline messageBlock: () -> Any?) {
    if (isInfoEnabled) info(messageBlock().toString())
}

inline fun Logger.warnLog(crossinline messageBlock: () -> Any?) {
    if (isWarnEnabled) warn(messageBlock().toString())
}

inline fun Logger.errorLog(crossinline messageBlock: () -> Any?) {
    if (isErrorEnabled) error(messageBlock().toString())
}

inline fun Logger.errorLog(
    throwable: Throwable,
    crossinline messageBlock: () -> Any?,
) {
    if (isErrorEnabled) error(messageBlock().toString(), throwable)
}

inline fun Logger.warnLog(
    throwable: Throwable,
    crossinline messageBlock: () -> Any?,
) {
    if (isWarnEnabled) warn(messageBlock().toString(), throwable)
}

inline fun Logger.infoLog(
    throwable: Throwable,
    crossinline messageBlock: () -> Any?,
) {
    if (isInfoEnabled) info(messageBlock().toString(), throwable)
}

inline fun Logger.debugLog(
    throwable: Throwable,
    crossinline messageBlock: () -> Any?,
) {
    if (isDebugEnabled) debug(messageBlock().toString(), throwable)
}

inline fun Logger.traceLog(
    throwable: Throwable,
    crossinline messageBlock: () -> Any?,
) {
    if (isTraceEnabled) trace(messageBlock().toString(), throwable)
}
