package xyz.shoaky.sourcedownloader.repo.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessingRecordRepository : JpaRepository<ProcessingRecord, Long> {

    fun findByProcessorNameAndRenameTimesLessThan(processorName: String, renameTimesTh: Int): List<ProcessingRecord>

    fun findByProcessorNameAndSourceItemHashing(processorName: String, sourceItemHashing: String): ProcessingRecord?
}