create table REF_REPAIR (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CAR_ID varchar(36),
    INSURANCE_CASE_ID varchar(36),
    DESCRIPTION varchar(255),
    REPAIR_DATE date,
    DB1_CUSTOMER_ID bigint,
    --
    primary key (ID)
);