alter table REF_DOC add constraint FK_REF_DOC_ON_CARD foreign key (CARD_ID) references REF_CARD(ID) on delete CASCADE;
