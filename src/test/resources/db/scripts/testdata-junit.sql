INSERT INTO relation_type(id, name, display_name)
VALUES ('rt1', "type-1", "type_display_name-1"),
       ('rt2', "counter_type-1", "counter_type_display_name-1"),
       ('rt3', "type-2", "type_display_name-2"),
       ('rt4', "counter_type-2", "counter_type_display_name-2"),
       ('rt5', "type-3", "type_display_name-3"),
       ('rt6', "counter_type-3", "counter_type_display_name-3");

UPDATE relation_type SET counter_type_id = 'rt2' WHERE id = 'rt1';
UPDATE relation_type SET counter_type_id = 'rt1' WHERE id = 'rt2';
UPDATE relation_type SET counter_type_id = 'rt4' WHERE id = 'rt3';
UPDATE relation_type SET counter_type_id = 'rt3' WHERE id = 'rt4';
UPDATE relation_type SET counter_type_id = 'rt5' WHERE id = 'rt6';
UPDATE relation_type SET counter_type_id = 'rt6' WHERE id = 'rt5';

INSERT INTO resource_identifier(id, resource_id, type, service, namespace)
VALUES
  (100, 'source_id-1', 'source_type-1', 'source_source-1', 'source_namespace-1'),
  (101, 'target_id-1', 'target_type-1', 'target_source-1', 'target_namespace-1'),
  (200, 'source_id-2', 'source_type-2', 'source_source-2', 'source_namespace-2'),
  (201, 'target_id-2', 'target_type-2', 'target_source-2', 'target_namespace-2');

INSERT INTO relation(id, municipality_id, type_id, created, modified, resource_source_identifier_id, resource_target_identifier_id)
VALUES
  (1, '2281', 'rt1', '2025-01-01 12:00:00.000', '2025-01-01 13:00:00.000', 100, 101),
  (2, '2281', 'rt2', '2025-01-01 12:00:00.000', '2025-01-01 13:00:00.000', 101, 100),
  (3, '2281', 'rt3', '2025-02-02 12:00:00.000', '2025-02-02 13:00:00.000', 200, 201),
  (4, '2281', 'rt4', '2025-02-02 12:00:00.000', '2025-02-02 13:00:00.000', 201, 200);

UPDATE relation SET inverse_relation_id = 2 WHERE id = 1;
UPDATE relation SET inverse_relation_id = 1 WHERE id = 2;





