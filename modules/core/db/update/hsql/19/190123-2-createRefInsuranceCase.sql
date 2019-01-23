alter table REF_INSURANCE_CASE add constraint FK_REF_INSURANCE_CASE_ON_CAR foreign key (CAR_ID) references REF_CAR(ID);
create index IDX_REF_INSURANCE_CASE_ON_CAR on REF_INSURANCE_CASE (CAR_ID);
