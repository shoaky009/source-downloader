package io.github.shoaky.sourcedownloader.core

import com.google.common.cache.CacheBuilder
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.zip.CRC32

class CachedVariableProvider(
    private val variableProvider: VariableProvider,
) : VariableProvider by variableProvider {

    private val itemCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build<String, PatternVariables>()

    private val fileCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(1))
        .build<String, List<PatternVariables>>()

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return itemCache.get(sourceItem.hashing()) {
            variableProvider.itemVariables(sourceItem)
        }
    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>
    ): List<PatternVariables> {
        val crc32 = CRC32()
        crc32.update(itemVariables.variables().toString().toByteArray(Charsets.UTF_8))
        sourceFiles.forEach {
            crc32.update(it.path.toString().toByteArray(Charsets.UTF_8))
            if (it.downloadUri != null) {
                crc32.update(it.downloadUri.toString().toByteArray(Charsets.UTF_8))
            }
            if (it.attrs.isNotEmpty()) {
                crc32.update(it.attrs.toString().toByteArray(Charsets.UTF_8))
            }
            if (it.tags.isNotEmpty()) {
                crc32.update(it.tags.toString().toByteArray(Charsets.UTF_8))
            }
        }
        val key = sourceItem.hashing() + crc32.value
        log.debug("item:{} iterVariables:{} file:{} cacheKey:{}", sourceItem, itemVariables, sourceFiles, key)
        return fileCache.get(key) {
            variableProvider.fileVariables(sourceItem, itemVariables, sourceFiles)
        }
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        return itemCache.get(sourceItem.hashing() + text) {
            variableProvider.extractFrom(sourceItem, text) ?: PatternVariables.EMPTY
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(CachedVariableProvider::class.java)
    }
}