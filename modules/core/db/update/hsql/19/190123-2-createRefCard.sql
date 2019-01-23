alter table REF_CARD add constraint FK_REF_CARD_ON_CREATOR foreign key (CREATOR_ID) references SEC_USER(ID);
alter table REF_CARD add constraint FK_REF_CARD_ON_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER(ID);
alter table REF_CARD add constraint FK_REF_CARD_ON_PARENT_CARD foreign key (PARENT_CARD_ID) references REF_CARD(ID);
alter table REF_CARD add constraint FK_REF_CARD_ON_CATEGORY foreign key (CATEGORY_ID) references SYS_CATEGORY(ID);
create index IDX_REF_CARD_ON_CREATOR on REF_CARD (CREATOR_ID);
create index IDX_REF_CARD_ON_SUBSTITUTED_CREATOR on REF_CARD (SUBSTITUTED_CREATOR_ID);
create index IDX_REF_CARD_ON_PARENT_CARD on REF_CARD (PARENT_CARD_ID);
create index IDX_REF_CARD_ON_CATEGORY on REF_CARD (CATEGORY_ID);
