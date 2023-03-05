package xyz.shoaky.sourcedownloader.repo.jpa

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class TargetPathRecord {
    @Id
    lateinit var id: String
    lateinit var createTime: LocalDateTime
}