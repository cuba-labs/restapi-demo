alter table REF_PLANT add constraint FK_REF_PLANT_ON_DOC foreign key (DOC_ID) references REF_DOC(CARD_ID);
create index IDX_REF_PLANT_ON_DOC on REF_PLANT (DOC_ID);
