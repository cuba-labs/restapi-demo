create table REF_CK_CUSTOMER (
    UUID varchar(36),
    --
    TENANT_ID integer,
    ENTITY_ID bigint,
    --
    NAME varchar(255),
    --
    primary key (TENANT_ID, ENTITY_ID)
);