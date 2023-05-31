package xyz.shoaky.sourcedownloader.repo.jpa

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import xyz.shoaky.sourcedownloader.core.PersistentItemPointer
import java.time.LocalDateTime

@Entity
@Table(
    name = "processor_source_state_record",
    uniqueConstraints = [
        UniqueConstraint(name = "uidx_processorname_sourceid", columnNames = ["processor_name", "source_id"])
    ])
class ProcessorSourceStateRecord {

    @Id
    @GeneratedValue(generator = "processor_source_state_record")
    // @SequenceGenerator(name = "sqlite_sequence", sequenceName = "processor_source_state_seq")
    @TableGenerator(name = "processor_source_state_record", table = "sqlite_sequence", valueColumnName = "seq",
        pkColumnName = "name", pkColumnValue = "processor_source_state_record")
    var id: Long? = null

    @Column(name = "processor_name")
    lateinit var processorName: String

    @Column(name = "source_id")
    lateinit var sourceId: String

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    lateinit var lastPointer: PersistentItemPointer
    var retryTimes: Int = 0
    var lastActiveTime: LocalDateTime = LocalDateTime.now()
}