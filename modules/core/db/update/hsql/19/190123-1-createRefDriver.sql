create table REF_DRIVER (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    COUNTRY varchar(255),
    STATE varchar(255),
    CITY varchar(255),
    STREET varchar(255),
    ZIP varchar(255),
    HOUSENUMBER varchar(255),
    FLATNUMBER varchar(255),
    LATITUDE double precision,
    LONGITUDE double precision,
    ADDRESS_SINCE date,
    PLACE_ID varchar(36),
    --
    NAME varchar(255),
    CALLSIGN_ID varchar(36),
    DRIVER_GROUP_ID varchar(36),
    STATUS integer,
    PLATFORM_ENTITY_ID varchar(36),
    --
    primary key (ID)
);