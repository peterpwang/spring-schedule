CREATE TABLE users
(
  id int NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  password varchar(255),
  description varchar(255),
  active int NOT NULL,
  version int,
  manager_id int,
  primary key (id)
);

CREATE TABLE managers
(
  id int NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  password varchar(255),
  description varchar(255),
  active int NOT NULL default 0,
  roles binary(255),
  version int,
  primary key (id)
);

CREATE TABLE schedules
(
  id int NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  date_schedule int,
  time_start int,
  time_end int,
  description varchar(255),
  active int NOT NULL,
  version int,
  user_id int,
  manager_id int,
  primary key (id)
);


insert into managers
  (id, name, password, description, active, roles, version)
  values( 1, 'john', '$10$IA5vAWgv.UeXeC6s8zu8OetG64WbcyTNSeWcKpablGnhILJh9X20O', NULL, 1, NULL, 0);
insert into managers
  (id, name, password, description, active, roles, version)
  values( 2, 'peter', '$10$IA5vAWgv.UeXeC6s8zu8OetG64WbcyTNSeWcKpablGnhILJh9X20O', NULL, 1, NULL, 0);

