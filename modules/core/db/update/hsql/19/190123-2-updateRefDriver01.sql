alter table REF_DRIVER add constraint FK_REF_DRIVER_ON_PLACE foreign key (PLACE_ID) references REF_PLACE(ID);
create index IDX_REF_DRIVER_ON_PLACE on REF_DRIVER (PLACE_ID);
