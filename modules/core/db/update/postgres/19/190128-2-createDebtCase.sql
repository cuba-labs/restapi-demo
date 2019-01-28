alter table DEBT_CASE add constraint FK_DEBT_CASE_ON_DEBTOR foreign key (DEBTOR_ID) references DEBT_DEBTOR(ID);
create index IDX_DEBT_CASE_ON_DEBTOR on DEBT_CASE (DEBTOR_ID);
