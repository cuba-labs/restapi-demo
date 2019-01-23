create table ORDER_ (
    CK_CUSTOMER_TENANT_ID integer,
    CK_CUSTOMER_ENTITY_ID bigint,
    --
    ID bigint not null,
    ORDER_DATE timestamp,
    CUSTOMER_ID bigint,
    MEM_CUST_ID varchar(36),
    IK_CUST_ID bigint,
    IK_ORDER_ID bigint,
    --
    primary key (ID)
);