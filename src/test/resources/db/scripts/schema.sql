
    create table relation (
        created datetime(6),
        modified datetime(6),
        id varchar(255) not null,
        inverse_relation_id varchar(255),
        municipality_id varchar(255) not null,
        resource_source_identifier_id varchar(255) not null,
        resource_target_identifier_id varchar(255) not null,
        type_id varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table relation_type (
        counter_type_id varchar(255),
        display_name varchar(255),
        id varchar(255) not null,
        name varchar(255) not null,
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

    alter table if exists relation
       add constraint uq_relation_inverse_relation_id unique (inverse_relation_id);

    alter table if exists relation_type
       add constraint uq_relation_type_name unique (name);

    alter table if exists relation_type
       add constraint uq_relation_type_counter_type_id unique (counter_type_id);

    alter table if exists relation
       add constraint fk_relation_inverse_relation_relation
       foreign key (inverse_relation_id)
       references relation (id);

    alter table if exists relation
       add constraint fk_relation_source_resource_identifier
       foreign key (resource_source_identifier_id)
       references resource_identifier (id);

    alter table if exists relation
       add constraint fk_relation_target_resource_identifier
       foreign key (resource_target_identifier_id)
       references resource_identifier (id);

    alter table if exists relation
       add constraint fk_relation_type_relation_type
       foreign key (type_id)
       references relation_type (id);

    alter table if exists relation_type
       add constraint fk_relation_type_counter_type_relation_type
       foreign key (counter_type_id)
       references relation_type (id);
