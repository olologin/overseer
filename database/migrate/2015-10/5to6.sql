-- Migration from state 5 to state 6
-- Author atokar
create table if not exists version(id bigint not null);
delete from version;
insert into version(id) values (6);
    

