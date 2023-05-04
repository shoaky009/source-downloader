package xyz.shoaky.sourcedownloader.repo.jpa

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import xyz.shoaky.sourcedownloader.core.PersistentItemPointer
import java.time.LocalDateTime

@Entity
@Table(uniqueConstraints = [
    UniqueConstraint(name = "UIDX_PROCESSORNAME_SOURCEID", columnNames = ["processorName", "sourceId"])
])
class ProcessorSourceStateRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "PROCESSING_RECORD_SEQ", sequenceName = "PROCESSING_RECORD_SEQ")
    var id: Long? = null
    lateinit var processorName: String
    lateinit var sourceId: String

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    lateinit var lastPointer: PersistentItemPointer
    var retryTimes: Int = 0
    var lastActiveTime: LocalDateTime = LocalDateTime.now()
}