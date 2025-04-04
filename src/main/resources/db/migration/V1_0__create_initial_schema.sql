create table relation (
    created datetime(6),
    modified datetime(6),
    id varchar(255) not null,
    municipality_id varchar(255) not null,
    resource_source_identifier_id varchar(255) not null,
    resource_target_identifier_id varchar(255) not null,
    type varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create table relation_type (
    counter_type varchar(255),
    counter_type_display_name varchar(255),
    id varchar(255) not null,
    type varchar(255) not null,
    type_display_name varchar(255),
    primary key (id)
) engine=InnoDB;

create table resource_identifier (
    modified datetime(6),
    id varchar(255) not null,
    namespace varchar(255),
    resource_id varchar(255) not null,
    service varchar(255) not null,
    type varchar(255) not null,
    primary key (id)
) engine=InnoDB;

alter table if exists relation
   add constraint uq_relation_resource_source_identifier_id unique (resource_source_identifier_id);

alter table if exists relation
   add constraint uq_relation_counter_resource_target_identifier_id unique (resource_target_identifier_id);

alter table if exists relation_type
   add constraint uq_relation_type unique (type);

alter table if exists relation_type
   add constraint uq_relation_counter_type unique (counter_type);

alter table if exists relation
   add constraint fk_relation_source_resource_identifier
   foreign key (resource_source_identifier_id)
   references resource_identifier (id);

alter table if exists relation
   add constraint fk_relation_target_resource_identifier
   foreign key (resource_target_identifier_id)
   references resource_identifier (id);
