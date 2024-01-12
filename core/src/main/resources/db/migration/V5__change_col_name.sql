ALTER TABLE processing_record
    RENAME COLUMN source_item_hashing TO item_hash;

CREATE INDEX idx_processorname_createtime
    ON processing_record (processor_name, create_time);