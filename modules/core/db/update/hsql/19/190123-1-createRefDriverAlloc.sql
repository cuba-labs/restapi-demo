create table REF_DRIVER_ALLOC (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    --
    DRIVER_ID varchar(36),
    CAR_ID varchar(36),
    --
    primary key (ID)
);