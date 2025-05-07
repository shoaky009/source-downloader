ALTER TABLE processing_record
    ADD COLUMN item_identity varchar(256) DEFAULT NULL;

CREATE INDEX idx_processorname_itemidentity ON processing_record (processor_name, item_identity);