package io.github.shoaky.sourcedownloader.sdk

interface FileStatus {

    fun status(): String

    fun isSuccessful(): Boolean
}