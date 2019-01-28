create table DEBT_CASE (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    TEST1 varchar(50),
    TEST2 varchar(50),
    TEST3 varchar(50),
    TEST4 varchar(50),
    TEST5 varchar(50),
    TEST6 varchar(50),
    TEST7 varchar(50),
    TEST8 varchar(50),
    TEST9 varchar(50),
    TEST10 varchar(50),
    DEBTOR_ID uuid not null,
    --
    primary key (ID)
);