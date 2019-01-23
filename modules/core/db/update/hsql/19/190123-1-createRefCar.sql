create table REF_CAR (
    ID varchar(36) not null,
    CATEGORY_ID varchar(36),
    VERSION integer,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    VIN varchar(255),
    COLOUR_ID varchar(36),
    MODEL_ID varchar(36),
    CAR_DOCUMENTATION_ID varchar(36),
    TOKEN_ID varchar(36),
    SELLER_ID bigint,
    CURRENCY_CODE varchar(255),
    --
    primary key (ID)
);