package xyz.shoaky.sourcedownloader.repo.jpa

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import java.time.LocalDateTime

@Entity
@Table(uniqueConstraints = [
    UniqueConstraint(name = "UIDX_PROCESSORNAME_SOURCEITEMHASHING", columnNames = ["processorName", "sourceItemHashing"])
])
class ProcessingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "PROCESSING_RECORD_SEQ", sequenceName = "PROCESSING_RECORD_SEQ")
    var id: Long? = null
    lateinit var processorName: String
    lateinit var sourceItemHashing: String

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    lateinit var sourceContent: PersistentSourceContent

    var renameTimes: Int = 0
    var status: Int = 0
    var failureReason: String? = null
    var modifyTime: LocalDateTime? = null
    lateinit var createTime: LocalDateTime
}