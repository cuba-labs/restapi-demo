create table REF_CAR_GARAGE_TOKEN (
    ID varchar(36) not null,
    --
    TITLE varchar(50) not null,
    TOKEN varchar(20) not null,
    DESCRIPTION varchar(200),
    LAST_USAGE timestamp,
    --
    primary key (ID)
);