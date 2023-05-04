package xyz.shoaky.sourcedownloader.repo.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessorSourceStateRepository : JpaRepository<ProcessorSourceStateRecord, String> {

    fun findByProcessorNameAndSourceId(processorName: String, sourceId: String): ProcessorSourceStateRecord?
}