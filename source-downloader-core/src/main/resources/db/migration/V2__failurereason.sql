// PROCESSING_RECORD add column failure_reason varchar(255) not null default '';
ALTER TABLE PROCESSING_RECORD
    ADD COLUMN FAILURE_REASON text NULL;