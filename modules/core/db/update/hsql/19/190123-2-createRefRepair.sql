alter table REF_REPAIR add constraint FK_REF_REPAIR_ON_CAR foreign key (CAR_ID) references REF_CAR(ID);
alter table REF_REPAIR add constraint FK_REF_REPAIR_ON_INSURANCE_CASE foreign key (INSURANCE_CASE_ID) references REF_INSURANCE_CASE(ID);
create index IDX_REF_REPAIR_ON_CAR on REF_REPAIR (CAR_ID);
create index IDX_REF_REPAIR_ON_INSURANCE_CASE on REF_REPAIR (INSURANCE_CASE_ID);
