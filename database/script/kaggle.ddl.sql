drop table if exists room cascade;
create table room
(
    idroom serial
        constraint room_pk
            primary key,
    name   varchar not null unique
);

drop table if exists co2 cascade;
create table co2
(
    idroom      int references room (idroom),
    timestamp   int,
    measurement float
);

drop table if exists humidity cascade;
create table humidity
(
    idroom      int references room (idroom),
    timestamp   int,
    measurement float
);
drop table if exists light cascade;
create table light
(
    idroom      int references room (idroom),
    timestamp   int,
    measurement float
);

drop table if exists pir cascade;
create table pir
(
    idroom      int references room (idroom),
    timestamp   int,
    measurement float
);

drop table if exists temperature cascade;
create table temperature
(
    idroom      int references room (idroom),
    timestamp   int,
    measurement float
);
