DROP TABLE IF EXISTS statistics;

CREATE TABLE IF NOT EXISTS statistics (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app VARCHAR(100) NOT NULL,
    uri VARCHAR(250) NOT NULL,
    ip VARCHAR(50) NOT NULL,
    time TIMESTAMP,
    CONSTRAINT pk_statistic PRIMARY KEY(id)
);