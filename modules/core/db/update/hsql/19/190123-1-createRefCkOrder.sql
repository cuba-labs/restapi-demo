create table REF_CK_ORDER (
    CUSTOMER_TENANT_ID integer,
    CUSTOMER_ENTITY_ID bigint,
    --
    TENANT_ID integer,
    ENTITY_ID bigint,
    --
    ORDER_DATE timestamp,
    --
    primary key (TENANT_ID, ENTITY_ID)
);