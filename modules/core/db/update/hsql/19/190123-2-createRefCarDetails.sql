alter table REF_CAR_DETAILS add constraint FK_REF_CAR_DETAILS_ON_CAR foreign key (CAR_ID) references REF_CAR(ID);
create index IDX_REF_CAR_DETAILS_ON_CAR on REF_CAR_DETAILS (CAR_ID);
