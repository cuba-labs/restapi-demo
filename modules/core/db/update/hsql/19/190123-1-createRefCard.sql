create table REF_CARD (
    ID varchar(36) not null,
    CATEGORY_ID varchar(36),
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
    CREATOR_ID varchar(36),
    SUBSTITUTED_CREATOR_ID varchar(36),
    PARENT_CARD_ID varchar(36),
    HAS_ATTACHMENTS boolean,
    HAS_ATTRIBUTES boolean,
    PARENT_CARD_ACCESS boolean,
    --
    primary key (ID)
);