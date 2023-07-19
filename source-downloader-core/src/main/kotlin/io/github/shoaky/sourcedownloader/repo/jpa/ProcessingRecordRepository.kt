package io.github.shoaky.sourcedownloader.repo.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProcessingRecordRepository : JpaRepository<ProcessingRecord, Long> {

    @Query("SELECT p FROM ProcessingRecord p WHERE p.processorName = ?1 AND p.renameTimes < ?2 AND p.status = 0")
    fun findByProcessorNameAndRenameTimesLessThan(processorName: String, renameTimesTh: Int): List<ProcessingRecord>

    fun findByProcessorNameAndSourceItemHashing(processorName: String, sourceItemHashing: String): ProcessingRecord?

    /**
     * For Testing, do not use in production
     */
    fun findByProcessorName(processorName: String): List<ProcessingRecord>

    fun findBySourceItemHashingIn(itemHashing: List<String>): List<ProcessingRecord>
}