alter table REF_IK_ORDER_LINE add constraint FK_REF_IK_ORDER_LINE_ON_ORDER foreign key (ORDER_ID) references REF_IK_ORDER(ID);
create index IDX_REF_IK_ORDER_LINE_ON_ORDER on REF_IK_ORDER_LINE (ORDER_ID);
