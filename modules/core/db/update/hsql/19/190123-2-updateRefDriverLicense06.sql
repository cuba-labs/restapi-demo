alter table REF_DRIVER_LICENSE add constraint FK_REF_DRIVER_LICENSE_ON_CAR foreign key (CAR_ID) references REF_CAR(ID);
create index IDX_REF_DRIVER_LICENSE_ON_CAR on REF_DRIVER_LICENSE (CAR_ID);
