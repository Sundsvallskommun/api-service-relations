INSERT INTO relation_type(id, name, display_name)
VALUES ('34b52960-df99-42e8-9690-1cea88823cf7', "type-1", "type_display_name-1"),
       ('71756128-4672-422d-99d9-852e80ddb288', "counter_type-1", "counter_type_display_name-1"),
       ('6c52970d-a6f8-4dce-b563-f910632647d5', "type-2", "type_display_name-2"),
       ('978028c5-3720-4833-975e-f02ff3bf489c', "counter_type-2", "counter_type_display_name-2"),
       ('9e7059bc-1f0f-4c39-ad72-f919f819dfa7', "type-3", "type_display_name-3"),
       ('1bdb4dc7-d4a7-4041-bcf2-207a78ad63ca', "counter_type-3", "counter_type_display_name-3");

UPDATE relation_type SET counter_type_id = '71756128-4672-422d-99d9-852e80ddb288' WHERE id = '34b52960-df99-42e8-9690-1cea88823cf7';
UPDATE relation_type SET counter_type_id = '34b52960-df99-42e8-9690-1cea88823cf7' WHERE id = '71756128-4672-422d-99d9-852e80ddb288';
UPDATE relation_type SET counter_type_id = '978028c5-3720-4833-975e-f02ff3bf489c' WHERE id = '6c52970d-a6f8-4dce-b563-f910632647d5';
UPDATE relation_type SET counter_type_id = '6c52970d-a6f8-4dce-b563-f910632647d5' WHERE id = '978028c5-3720-4833-975e-f02ff3bf489c';
UPDATE relation_type SET counter_type_id = '9e7059bc-1f0f-4c39-ad72-f919f819dfa7' WHERE id = '1bdb4dc7-d4a7-4041-bcf2-207a78ad63ca';
UPDATE relation_type SET counter_type_id = '1bdb4dc7-d4a7-4041-bcf2-207a78ad63ca' WHERE id = '9e7059bc-1f0f-4c39-ad72-f919f819dfa7';

INSERT INTO resource_identifier(id, resource_id, type, service, namespace, modified)
VALUES
  (100, 'source_id-1', 'source_type-1', 'source_source-1', 'source_namespace-1', null),
  (101, 'target_id-1', 'target_type-1', 'target_source-1', 'target_namespace-1', '2025-01-01 14:00:00.000'),
  (200, 'source_id-2', 'source_type-2', 'source_source-2', 'source_namespace-2', null),
  (201, 'target_id-2', 'target_type-2', 'target_source-2', 'target_namespace-2', null);


INSERT INTO relation(id, municipality_id, type_id, created, modified, resource_source_identifier_id, resource_target_identifier_id)
VALUES
  ('9557fadb-14b1-4202-a933-a8ad3c0c07ab', '2281', '34b52960-df99-42e8-9690-1cea88823cf7', '2025-01-01 12:00:00.000', '2025-01-01 13:00:00.000', 100, 101),
  ('4bb60383-c522-4187-ad43-f7a29635fc14', '2281', '71756128-4672-422d-99d9-852e80ddb288', '2025-01-01 12:00:00.000', '2025-01-01 13:00:00.000', 101, 100),
  ('42bccb87-9f0f-4a69-b0ff-af6d6aea3bcf', '2281', '6c52970d-a6f8-4dce-b563-f910632647d5', '2025-02-02 12:00:00.000', '2025-02-02 13:00:00.000', 200, 201),
  ('52295e51-aed6-463e-97c1-893acbb13cf0', '2281', '978028c5-3720-4833-975e-f02ff3bf489c', '2025-02-02 12:00:00.000', '2025-02-02 13:00:00.000', 201, 200);

UPDATE relation SET inverse_relation_id = '4bb60383-c522-4187-ad43-f7a29635fc14' WHERE id = '9557fadb-14b1-4202-a933-a8ad3c0c07ab';
UPDATE relation SET inverse_relation_id = '9557fadb-14b1-4202-a933-a8ad3c0c07ab' WHERE id = '4bb60383-c522-4187-ad43-f7a29635fc14';
UPDATE relation SET inverse_relation_id = '52295e51-aed6-463e-97c1-893acbb13cf0' WHERE id = '42bccb87-9f0f-4a69-b0ff-af6d6aea3bcf';
UPDATE relation SET inverse_relation_id = '42bccb87-9f0f-4a69-b0ff-af6d6aea3bcf' WHERE id = '52295e51-aed6-463e-97c1-893acbb13cf0';
