INSERT INTO relation(
  id, municipality_id, type, created, modified,
  source_id, source_type, source_service, source_namespace,
  target_id, target_type, target_service, target_namespace)
VALUES
  (1, '2281', 'type-1', '2025-01-01 12:00:00.000', '2025-01-01 13:00:00.000',
  'source_id-1', 'source_type-1', 'source_source-1', 'source_namespace-1',
  'target_id-1', 'target_type-1', 'target_source-1', 'target_namespace-1'),
  (2, '2281', 'type-2', '2025-02-02 12:00:00.000', '2025-02-02 13:00:00.000',
    'source_id-2', 'source_type-2', 'source_source-2', 'source_namespace-2',
    'target_id-2', 'target_type-2', 'target_source-2', 'target_namespace-2');

INSERT INTO relation_type(id, type, type_display_name, counter_type, counter_type_display_name)
VALUES ("1", "type-1", "type_display_name-1", "counter_type-1", "counter_type_display_name-1"),
       ("2", "type-2", "type_display_name-2", "counter_type-2", "counter_type_display_name-2");
