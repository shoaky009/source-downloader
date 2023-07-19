package io.github.shoaky.sourcedownloader.repo.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface TargetPathRepository : JpaRepository<TargetPathRecord, String> {

    fun existsAllByIdIn(ids: List<String>): Boolean

}