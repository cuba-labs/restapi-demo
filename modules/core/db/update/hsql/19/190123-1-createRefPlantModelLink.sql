create table REF_PLANT_MODEL_LINK (
    PLANT_ID varchar(36) not null,
    MODEL_ID varchar(36) not null,
    primary key (PLANT_ID, MODEL_ID)
);
