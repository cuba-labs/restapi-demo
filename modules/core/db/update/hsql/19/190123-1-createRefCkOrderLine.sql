create table REF_CK_ORDER_LINE (
    ORDER_TENANT_ID integer,
    ORDER_ENTITY_ID bigint,
    --
    TENANT_ID integer,
    ENTITY_ID bigint,
    --
    PRODUCT varchar(255),
    --
    primary key (TENANT_ID, ENTITY_ID)
);