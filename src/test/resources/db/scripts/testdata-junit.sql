INSERT INTO resource_identifier(id, resource_id, type, service, namespace)
VALUES
  (100, 'source_id-1', 'source_type-1', 'source_source-1', 'source_namespace-1'),
  (101, 'target_id-1', 'target_type-1', 'target_source-1', 'target_namespace-1'),
  (200, 'source_id-2', 'source_type-2', 'source_source-2', 'source_namespace-2'),
  (201, 'target_id-2', 'target_type-2', 'target_source-2', 'target_namespace-2');

INSERT INTO relation(id, municipality_id, type, created, modified, resource_source_identifier_id, resource_target_identifier_id)
VALUES
  (1, '2281', 'type-1', '2025-01-01 12:00:00.000', '2025-01-01 13:00:00.000', 100, 101),
  (2, '2281', 'type-2', '2025-02-02 12:00:00.000', '2025-02-02 13:00:00.000', 200, 201);


INSERT INTO relation_type(id, type, type_display_name, counter_type, counter_type_display_name)
VALUES ("1", "type-1", "type_display_name-1", "counter_type-1", "counter_type_display_name-1"),
       ("2", "type-2", "type_display_name-2", "counter_type-2", "counter_type_display_name-2");
