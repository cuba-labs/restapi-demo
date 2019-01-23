-- begin REF_CAR
create table REF_CAR (
    ID uuid,
    CATEGORY_ID uuid,
    VERSION integer,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    VIN varchar(255),
    COLOUR_ID uuid,
    MODEL_ID uuid,
    CAR_DOCUMENTATION_ID uuid,
    TOKEN_ID uuid,
    SELLER_ID bigint,
    CURRENCY_CODE varchar(255),
    --
    primary key (ID)
)^
-- end REF_CAR
-- begin REF_INSURANCE_CASE
create table REF_INSURANCE_CASE (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CAR_ID uuid,
    DESCRIPTION varchar(255),
    --
    primary key (ID)
)^
-- end REF_INSURANCE_CASE
-- begin REF_REPAIR
create table REF_REPAIR (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CAR_ID uuid,
    INSURANCE_CASE_ID uuid,
    DESCRIPTION varchar(255),
    REPAIR_DATE date,
    DB1_CUSTOMER_ID bigint,
    --
    primary key (ID)
)^
-- end REF_REPAIR
-- begin REF_CAR_TOKEN
create table REF_CAR_TOKEN (
    ID uuid,
    --
    TOKEN varchar(255),
    REPAIR_ID uuid,
    GARAGE_TOKEN_ID uuid,
    --
    primary key (ID)
)^
-- end REF_CAR_TOKEN
-- begin REF_CAR_GARAGE_TOKEN
create table REF_CAR_GARAGE_TOKEN (
    ID uuid,
    --
    TITLE varchar(50) not null,
    TOKEN varchar(20) not null,
    DESCRIPTION varchar(200),
    LAST_USAGE timestamp,
    --
    primary key (ID)
)^
-- end REF_CAR_GARAGE_TOKEN
-- begin REF_MODEL
create table REF_MODEL (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    MANUFACTURER varchar(255),
    NUMBER_OF_SEATS integer,
    --
    primary key (ID)
)^
-- end REF_MODEL
-- begin REF_PLANT
create table REF_PLANT (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    DOC_ID uuid,
    --
    primary key (ID)
)^
-- end REF_PLANT
-- begin REF_DOC
create table REF_DOC (
    CARD_ID uuid,
    --
    DOC_NUMBER varchar(50),
    AMOUNT decimal(19, 2),
    --
    primary key (CARD_ID)
)^
-- end REF_DOC
-- begin REF_CARD
create table REF_CARD (
    ID uuid,
    CATEGORY_ID uuid,
    VERSION integer,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_TYPE integer,
    --
    STATE varchar(255),
    DESCRIPTION varchar(1000),
    CREATOR_ID uuid,
    SUBSTITUTED_CREATOR_ID uuid,
    PARENT_CARD_ID uuid,
    HAS_ATTACHMENTS boolean,
    HAS_ATTRIBUTES boolean,
    PARENT_CARD_ACCESS boolean,
    --
    primary key (ID)
)^
-- end REF_CARD
-- begin REF_COLOUR
create table REF_COLOUR (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    DESCRIPTION varchar(255) not null,
    --
    primary key (ID)
)^
-- end REF_COLOUR
-- begin REF_CAR_DETAILS
create table REF_CAR_DETAILS (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CAR_ID uuid,
    DETAILS varchar(255),
    --
    primary key (ID)
)^
-- end REF_CAR_DETAILS
-- begin REF_CAR_DETAILS_ITEM
create table REF_CAR_DETAILS_ITEM (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CAR_DETAILS_ID uuid,
    INFO varchar(255),
    --
    primary key (ID)
)^
-- end REF_CAR_DETAILS_ITEM
-- begin REF_CAR_DOCUMENTATION
create table REF_CAR_DOCUMENTATION (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    TITLE varchar(255),
    --
    primary key (ID)
)^
-- end REF_CAR_DOCUMENTATION
-- begin REF_SELLER
create table REF_SELLER (
    ID bigint,
    UUID uuid,
    VERSION integer,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end REF_SELLER
-- begin ORDER_
create table ORDER_ (
    CK_CUSTOMER_TENANT_ID integer,
    CK_CUSTOMER_ENTITY_ID bigint,
    --
    ID bigint,
    ORDER_DATE timestamp,
    CUSTOMER_ID bigint,
    MEM_CUST_ID uuid,
    IK_CUST_ID bigint,
    IK_ORDER_ID bigint,
    --
    primary key (ID)
)^
-- end ORDER_
-- begin CUSTOMER
create table CUSTOMER (
    ID bigint,
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end CUSTOMER
-- begin REF_CK_ORDER
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
)^
-- end REF_CK_ORDER
-- begin REF_CK_CUSTOMER
create table REF_CK_CUSTOMER (
    UUID uuid,
    --
    TENANT_ID integer,
    ENTITY_ID bigint,
    --
    NAME varchar(255),
    --
    primary key (TENANT_ID, ENTITY_ID)
)^
-- end REF_CK_CUSTOMER
-- begin REF_CK_ORDER_LINE
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
)^
-- end REF_CK_ORDER_LINE
-- begin REF_CK_ORDER_LINE_TAG
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
)^
-- end REF_CK_ORDER_LINE_TAG
-- begin REF_IK_ORDER
create table REF_IK_ORDER (
    ID bigserial,
    --
    ORDER_DATE timestamp,
    CUSTOMER_ID bigint,
    --
    primary key (ID)
)^
-- end REF_IK_ORDER
-- begin REF_IK_CUSTOMER
create table REF_IK_CUSTOMER (
    ID bigserial,
    --
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end REF_IK_CUSTOMER
-- begin REF_IK_ORDER_LINE
create table REF_IK_ORDER_LINE (
    ID bigserial,
    --
    ORDER_ID bigint,
    PRODUCT varchar(255),
    --
    primary key (ID)
)^
-- end REF_IK_ORDER_LINE
-- begin REF_IK_ORDER_LINE_TAG
create table REF_IK_ORDER_LINE_TAG (
    ID bigserial,
    --
    ORDER_LINE_ID bigint,
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end REF_IK_ORDER_LINE_TAG
-- begin REF_DRIVER_ALLOC
create table REF_DRIVER_ALLOC (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    --
    DRIVER_ID uuid,
    CAR_ID uuid,
    --
    primary key (ID)
)^
-- end REF_DRIVER_ALLOC
-- begin REF_DRIVER
create table REF_DRIVER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    COUNTRY varchar(255),
    STATE varchar(255),
    CITY varchar(255),
    STREET varchar(255),
    ZIP varchar(255),
    HOUSENUMBER varchar(255),
    FLATNUMBER varchar(255),
    LATITUDE double precision,
    LONGITUDE double precision,
    ADDRESS_SINCE date,
    PLACE_ID uuid,
    --
    NAME varchar(255),
    CALLSIGN_ID uuid,
    DRIVER_GROUP_ID uuid,
    STATUS integer,
    PLATFORM_ENTITY_ID uuid,
    --
    primary key (ID)
)^
-- end REF_DRIVER
-- begin REF_PLACE
create table REF_PLACE (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end REF_PLACE
-- begin REF_DRIVER_LICENSE
create table REF_DRIVER_LICENSE (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    DRIVER_ID uuid not null,
    CAR_ID uuid not null,
    PRIORITY integer not null,
    --
    primary key (ID)
)^
-- end REF_DRIVER_LICENSE
-- begin REF_DRIVER_GROUP
create table REF_DRIVER_GROUP (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    IN_USE boolean,
    --
    primary key (ID)
)^
-- end REF_DRIVER_GROUP
-- begin REF_DRIVER_CALLSIGN
create table REF_DRIVER_CALLSIGN (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CALLSIGN varchar(50),
    --
    primary key (ID)
)^
-- end REF_DRIVER_CALLSIGN
-- begin REF_SAMPLE_PLATFORM_ENTITY
create table REF_SAMPLE_PLATFORM_ENTITY (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end REF_SAMPLE_PLATFORM_ENTITY
-- begin REF_CURRENCY
create table REF_CURRENCY (
    CODE varchar(255),
    UUID uuid,
    VERSION integer,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    --
    primary key (CODE)
)^
-- end REF_CURRENCY
-- begin REF_PLANT_MODEL_LINK
create table REF_PLANT_MODEL_LINK (
    PLANT_ID uuid,
    MODEL_ID uuid,
    primary key (PLANT_ID, MODEL_ID)
)^
-- end REF_PLANT_MODEL_LINK
