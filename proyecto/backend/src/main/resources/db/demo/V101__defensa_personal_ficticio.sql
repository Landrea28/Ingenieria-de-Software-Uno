INSERT INTO defensa_personal_simulado
    (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo)
SELECT '8000000005', 'Ana', 'Prueba5', 'POLICIA', id, 'DEMO-005', TRUE
FROM rango WHERE fuerza_codigo = 'POLICIA' AND nombre = 'Subintendente';

INSERT INTO defensa_personal_simulado
    (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo)
SELECT '8000000006', 'Luis', 'Prueba6', 'EJERCITO', id, 'DEMO-006', TRUE
FROM rango WHERE fuerza_codigo = 'EJERCITO' AND nombre = 'Cabo';

INSERT INTO defensa_personal_simulado
    (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo)
SELECT '8000000007', 'Mia', 'Prueba7', 'ARMADA', id, 'DEMO-007', TRUE
FROM rango WHERE fuerza_codigo = 'ARMADA' AND nombre = 'Cabo Primero';

INSERT INTO defensa_personal_simulado
    (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo)
SELECT '8000000008', 'Juan', 'Prueba8', 'FUERZA_AEREA', id, 'DEMO-008', TRUE
FROM rango WHERE fuerza_codigo = 'FUERZA_AEREA' AND nombre = 'Tecnico Primero';

INSERT INTO defensa_personal_simulado
    (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo)
SELECT '8000000009', 'Eva', 'Prueba9', 'POLICIA', id, 'DEMO-009', TRUE
FROM rango WHERE fuerza_codigo = 'POLICIA' AND nombre = 'Intendente';

INSERT INTO defensa_personal_simulado
    (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo)
SELECT '8000000010', 'Leo', 'Prueba10', 'EJERCITO', id, 'DEMO-010', TRUE
FROM rango WHERE fuerza_codigo = 'EJERCITO' AND nombre = 'Sargento';
