package io.github.shoaky.sourcedownloader.repo.jpa

import com.vladmihalcea.hibernate.type.json.JsonType
import io.github.shoaky.sourcedownloader.core.PersistentPointer
import jakarta.persistence.*
import org.hibernate.annotations.Type
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
    @TableGenerator(name = "processor_source_state_record", table = "sqlite_sequence", valueColumnName = "seq",
        pkColumnName = "name", pkColumnValue = "processor_source_state_record")
    var id: Long? = null

    @Column(name = "processor_name")
    lateinit var processorName: String

    @Column(name = "source_id")
    lateinit var sourceId: String

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    lateinit var lastPointer: PersistentPointer
    var retryTimes: Int = 0
    var lastActiveTime: LocalDateTime = LocalDateTime.now()
}