create table BOAST (
    ID varchar(50) not null,
    CANARY varchar(50) not null,
    SECRET varchar(50) not null,
    URI varchar(50) not null
);

create table INTERACTSH (
    CORRELATION_ID char(20) not null,
    SECRET_KEY uuid not null,
    SERVER_URL varchar(100) not null,
    ENCODED_PRIVATE_KEY varbinary(500) not null,
    ENCODED_PUBLIC_KEY varbinary(500) not null
);
