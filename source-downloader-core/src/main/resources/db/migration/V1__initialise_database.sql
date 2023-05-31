CREATE TABLE target_path_record
(
    id            CHARACTER VARYING PRIMARY KEY,
    processing_id INTEGER NULL,
    create_time   DATETIME
);

CREATE TABLE processing_record
(
    ID                  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    processor_name      VARCHAR(255)                      NOT NULL,
    source_item_hashing VARCHAR(64)                       NOT NULL,
    source_content      JSON,
    rename_times        INT      DEFAULT 0,
    status              INT      DEFAULT 0,
    failure_reason      TEXT                              NULL,
    modify_time         DATETIME DEFAULT NULL,
    create_time         DATETIME
);
CREATE UNIQUE INDEX uidx_processorname_sourceitemhashing ON processing_record (processor_name, source_item_hashing);
-- CREATE SEQUENCE processing_record_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE processor_source_state_record
(
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    processor_name   VARCHAR(255) NOT NULL,
    source_id        VARCHAR(64)  NOT NULL,
    last_pointer     JSON,
    retry_times      INT DEFAULT 0,
    last_active_time DATETIME
);

CREATE UNIQUE INDEX uidx_processorname_sourceid ON processor_source_state_record (processor_name, source_id);
-- CREATE processor_source_state_record_seq START WITH 1 INCREMENT BY 50;