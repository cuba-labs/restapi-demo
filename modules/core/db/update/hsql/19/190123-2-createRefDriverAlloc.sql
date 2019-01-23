alter table REF_DRIVER_ALLOC add constraint FK_REF_DRIVER_ALLOC_ON_DRIVER foreign key (DRIVER_ID) references REF_DRIVER(ID);
alter table REF_DRIVER_ALLOC add constraint FK_REF_DRIVER_ALLOC_ON_CAR foreign key (CAR_ID) references REF_CAR(ID);
create index IDX_REF_DRIVER_ALLOC_ON_DRIVER on REF_DRIVER_ALLOC (DRIVER_ID);
create index IDX_REF_DRIVER_ALLOC_ON_CAR on REF_DRIVER_ALLOC (CAR_ID);
