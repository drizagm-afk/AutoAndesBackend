-- Run in SQL Developer connected as autoandesdb.
-- For a successful POST /api/v1/ordenes, substitute its returned order number.
SELECT o.id, o.numero, o.fecha, v.placa, o.kilometraje, o.estado,
       o.total, o.usuario_registro, o.motivo_anulacion
FROM orden_servicio o JOIN vehiculo v ON v.id = o.vehiculo_id
ORDER BY o.fecha DESC, o.id DESC;

SELECT o.numero, s.codigo, d.cantidad, d.precio_unitario, d.subtotal
FROM detalle_orden d
JOIN orden_servicio o ON o.id = d.orden_id
JOIN servicio s ON s.id = d.servicio_id
ORDER BY o.id DESC, d.id;

-- Header amounts must equal the sum of their details. Expected: zero rows.
SELECT o.numero, o.total, SUM(d.subtotal) total_detalles
FROM orden_servicio o JOIN detalle_orden d ON d.orden_id = o.id
GROUP BY o.numero, o.total
HAVING o.total <> SUM(d.subtotal);

-- Capture both counts immediately before and after a POST whose THIRD detail
-- uses inactive SRV-008. The response must be 409 and both counts unchanged.
SELECT (SELECT COUNT(*) FROM orden_servicio) cabeceras,
       (SELECT COUNT(*) FROM detalle_orden) detalles FROM dual;

-- No orphan details or headers without lines. Both counts must be zero.
SELECT COUNT(*) detalles_huerfanos FROM detalle_orden d
LEFT JOIN orden_servicio o ON o.id = d.orden_id WHERE o.id IS NULL;
SELECT COUNT(*) cabeceras_sin_detalle FROM orden_servicio o
WHERE NOT EXISTS (SELECT 1 FROM detalle_orden d WHERE d.orden_id = o.id);

-- SQL equivalent of the report's JPQL aggregation. Canceled orders are excluded.
SELECT s.codigo, s.nombre servicio, SUM(d.cantidad) cantidad_total,
       SUM(d.subtotal) importe_total
FROM detalle_orden d JOIN servicio s ON s.id = d.servicio_id
JOIN orden_servicio o ON o.id = d.orden_id
WHERE o.estado = 'REGISTRADA'
GROUP BY s.codigo, s.nombre
ORDER BY importe_total DESC, s.codigo ASC;
