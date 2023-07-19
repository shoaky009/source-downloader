DROP INDEX IF EXISTS uidx_processorname_sourceitemhashing;
CREATE UNIQUE INDEX uidx_sourceitemhashing_processorname
    ON processing_record (source_item_hashing DESC, processor_name)