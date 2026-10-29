-- Seed orders occupy OS-000001 through OS-000004. Sequence values are never reused.
-- A rolled-back order consumes a number; gaps are expected and avoid concurrency collisions.
CREATE SEQUENCE orden_numero_seq START WITH 5 INCREMENT BY 1 MAXVALUE 999999 NOCYCLE CACHE 20;
