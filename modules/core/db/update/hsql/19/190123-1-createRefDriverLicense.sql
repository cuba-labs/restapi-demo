create table REF_DRIVER_LICENSE (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    DRIVER_ID varchar(36) not null,
    CAR_ID varchar(36) not null,
    PRIORITY integer not null,
    --
    primary key (ID)
);