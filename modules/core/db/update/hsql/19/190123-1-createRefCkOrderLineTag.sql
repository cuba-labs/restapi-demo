create table REF_CK_ORDER_LINE_TAG (
    ORDER_LINE_TENANT_ID integer,
    ORDER_LINE_ENTITY_ID bigint,
    --
    TENANT_ID integer,
    ENTITY_ID bigint,
    --
    NAME varchar(255),
    --
    primary key (TENANT_ID, ENTITY_ID)
);