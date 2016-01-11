drop table if exists person;
create table person(
id 			bigint 			not null auto_increment,
gender 		tinyint 		not null,
name 		varchar(40) 	not null,
tel 		varchar(50),
create_time timestamp		not null default "0000-00-00 00:00:00",
modify_time	timestamp		not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
primary key(id)
);


drop table if exists car;
create table car(
id 			bigint 			not null auto_increment,
brand 		varchar(40) 	not null,
name 		varchar(40) 	not null,
manufacture	varchar(50),
owner_id	bigint,
create_time timestamp		not null default "0000-00-00 00:00:00",
modify_time	timestamp		not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
primary key(id),
foreign key(owner_id) references person(id)
);