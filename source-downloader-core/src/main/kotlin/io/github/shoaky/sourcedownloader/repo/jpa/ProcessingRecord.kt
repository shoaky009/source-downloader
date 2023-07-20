package io.github.shoaky.sourcedownloader.repo.jpa

import com.vladmihalcea.hibernate.type.json.JsonType
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.LocalDateTime

@Entity
@Table(
    name = "processing_record",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uidx_processorname_sourceitemhashing",
            columnNames = ["processor_name", "source_item_hashing"]
        )
    ])
class ProcessingRecord {

    @Id
    @GeneratedValue(generator = "processing_record")
    @TableGenerator(name = "processing_record", table = "sqlite_sequence", valueColumnName = "seq",
        pkColumnName = "name", pkColumnValue = "processing_record")
    var id: Long? = null

    @Column(name = "processor_name")
    lateinit var processorName: String

    @Column(name = "source_item_hashing")
    lateinit var sourceItemHashing: String

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    lateinit var itemContent: CoreItemContent

    var renameTimes: Int = 0
    var status: Int = 0
    var failureReason: String? = null
    var modifyTime: LocalDateTime? = null
    lateinit var createTime: LocalDateTime
}