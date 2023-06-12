package io.github.shoaky.sourcedownloader.external.transmission

data class TransmissionResponse<R>(
    val arguments: R,
    val result: String = "failed",
    val tag: Long
) {
    fun isSuccess(): Boolean {
        return result == SUCCESS
    }

    companion object {
        private const val SUCCESS = "success"
    }
}

data class TorrentGetResponse(
    val torrents: List<Torrent>
)