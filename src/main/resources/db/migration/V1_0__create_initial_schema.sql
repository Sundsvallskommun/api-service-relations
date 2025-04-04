create table relation (
    created datetime(6),
    modified datetime(6),
    id varchar(255) not null,
    municipality_id varchar(255) not null,
    source_id varchar(255) not null,
    source_namespace varchar(255),
    source_service varchar(255) not null,
    source_type varchar(255) not null,
    target_id varchar(255) not null,
    target_namespace varchar(255),
    target_service varchar(255) not null,
    target_type varchar(255) not null,
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

alter table if exists relation_type
   add constraint uq_relation_type unique (type);

alter table if exists relation_type
   add constraint uq_relation_counter_type unique (counter_type);
