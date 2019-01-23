alter table ORDER_ add constraint FK_ORDER__ON_CUSTOMER foreign key (CUSTOMER_ID) references CUSTOMER(ID);
create index IDX_ORDER__ON_CUSTOMER on ORDER_ (CUSTOMER_ID);
