package io.github.shoaky.sourcedownloader.common.pixiv

import io.github.shoaky.sourcedownloader.sdk.ItemPointer

data class IllustrationPointer(
    val illustrationId: Long,
    val userId: Long,
) : ItemPointer