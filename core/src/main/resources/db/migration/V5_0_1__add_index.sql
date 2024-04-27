CREATE INDEX idx_processorname_id
    ON processing_record (processor_name, id DESC);

create index idx_createtime
    on processing_record (create_time desc);