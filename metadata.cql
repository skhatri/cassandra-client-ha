create keyspace metadata with replication = {'class': 'NetworkTopologyStrategy', 'dc1': '1', 'dc2': '1'};

create table metadata.objects(
    name text primary key,
    object_type text,
    parent text,
    description text
);

insert into metadata.objects(name,object_type, parent, description)
values('customer', 'table', 'schema1', 'Customer Data');

insert into metadata.objects(name,object_type, parent, description)
values('name', 'column', 'customer', 'Name column');

insert into metadata.objects(name,object_type, parent, description)
values('address', 'column', 'customer', 'Address column');

insert into metadata.objects(name,object_type, parent, description)
values('phone', 'column', 'customer', 'Phone number');

insert into metadata.objects(name,object_type, parent, description)
values('preferences', 'table', 'schema1', 'Preferences table');

insert into metadata.objects(name,object_type, parent, description)
values('namespace', 'column', 'preferences', 'Preference namespace');

insert into metadata.objects(name,object_type, parent, description)
values('key', 'column', 'preferences', 'Preference Key');

insert into metadata.objects(name,object_type, parent, description)
values('value', 'column', 'preferences', 'Preferences value');

