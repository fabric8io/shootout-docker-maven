-- This DDL is used by flyway to initially setup the table
-- which will hold the log entries
--


-- Simple table for holding logs
create table LOGGING (
 -- Timestamp for log entry
    date TIMESTAMP null,
 -- IP address for the client
    IP varchar(100) not null,
 -- Client's URL
    URL varchar(200) not null
);