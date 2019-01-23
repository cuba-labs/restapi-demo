create table REF_CAR_TOKEN (
    ID varchar(36) not null,
    --
    TOKEN varchar(255),
    REPAIR_ID varchar(36),
    GARAGE_TOKEN_ID varchar(36),
    --
    primary key (ID)
);