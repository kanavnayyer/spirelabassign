package com.kanav.spirelabassign.ui.common

fun Throwable.toUserMessage(): String = when (this) {
    is java.net.UnknownHostException,
    is java.net.ConnectException -> "No internet connection"
    is java.net.SocketTimeoutException -> "Request timed out. Please try again."
    else -> localizedMessage ?: "Something went wrong"
}
