create table REF_DOC (
    CARD_ID varchar(36) not null,
    --
    DOC_NUMBER varchar(50),
    AMOUNT decimal(19, 2),
    --
    primary key (CARD_ID)
);