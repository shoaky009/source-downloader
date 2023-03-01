package xyz.shoaky.sourcedownloader.core

object MemProcessingStorage : ProcessingStorage {

    private val list = mutableListOf<ProcessingContent>()
    override fun saveRenameTask(content: ProcessingContent) {
        list.add(content)
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return list.filter { it.processorName == name && it.renameTimes < renameTimesThreshold }
    }

    override fun deleteById(id: Long) {
        list.removeIf { it.id == id }
    }

    override fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent? {
        return list.firstOrNull { it.processorName == processorName && it.sourceHash == itemHashing }
    }
}