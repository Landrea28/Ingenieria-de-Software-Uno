INSERT INTO ciudadano (identificacion, nombres, apellidos, fecha_nacimiento, lugar_nacimiento, fecha_expedicion, lugar_expedicion, rh, grupo_sanguineo, estatura) VALUES
('9000000001', 'Clara', 'Rivera', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'B', 1.71),
('9000000002', 'Luis', 'Torres', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'AB', 1.72),
('9000000003', 'Sara', 'Castro', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'O', 1.73),
('9000000004', 'Andres', 'Pardo', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'A', 1.74),
('9000000005', 'Marta', 'Luna', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'B', 1.75),
('9000000006', 'Daniel', 'Salazar', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'AB', 1.76),
('9000000007', 'Elena', 'Bustos', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'O', 1.77),
('9000000008', 'Pedro', 'Silva', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'A', 1.78),
('9000000009', 'Sofia', 'Rojas', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'B', 1.79),
('9000000010', 'Julian', 'Vega', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'AB', 1.70),
('9000000011', 'Diana', 'Acosta', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'O', 1.71),
('9000000012', 'Camilo', 'Nieto', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'A', 1.72),
('9000000013', 'Paula', 'Peña', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'B', 1.73),
('9000000014', 'Esteban', 'Suarez', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'AB', 1.74),
('9000000015', 'Laura', 'Mora', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'O', 1.75),
('9000000016', 'Mateo', 'Bernal', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'A', 1.76),
('9000000017', 'Carolina', 'Mejia', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'B', 1.77),
('9000000018', 'Natalia', 'Santos', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'AB', 1.78),
('9000000019', 'Mario', 'Quintero', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '+', 'O', 1.79),
('9000000020', 'Valeria', 'Campos', '1995-02-14', 'Bogota', '2013-03-22', 'Bogota', '-', 'A', 1.70);

INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000001';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000002';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000003';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000004';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000005';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000006';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000007';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000008';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000009';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000010';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000011';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000012';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000013';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000014';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000015';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000016';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000017';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000018';
INSERT INTO antecedente (ciudadano_id, estado, fuente, fecha_actualizacion) SELECT id, 'NO_REQUERIDO', 'SIMULADA', CURRENT_TIMESTAMP FROM ciudadano WHERE identificacion = '9000000019';

INSERT INTO defensa_personal_simulado (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo) SELECT '8000000001', 'Usuario', 'Prueba1', 'POLICIA', id, 'DEMO-001', TRUE FROM rango WHERE fuerza_codigo = 'POLICIA' AND nombre = 'Patrullero';
INSERT INTO defensa_personal_simulado (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo) SELECT '8000000002', 'Usuario', 'Prueba2', 'EJERCITO', id, 'DEMO-002', TRUE FROM rango WHERE fuerza_codigo = 'EJERCITO' AND nombre = 'Soldado';
INSERT INTO defensa_personal_simulado (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo) SELECT '8000000003', 'Usuario', 'Prueba3', 'ARMADA', id, 'DEMO-003', TRUE FROM rango WHERE fuerza_codigo = 'ARMADA' AND nombre = 'Marinero';
INSERT INTO defensa_personal_simulado (identificacion, nombres, apellidos, fuerza_codigo, rango_id, id_fuerza, activo) SELECT '8000000004', 'Usuario', 'Prueba4', 'FUERZA_AEREA', id, 'DEMO-004', TRUE FROM rango WHERE fuerza_codigo = 'FUERZA_AEREA' AND nombre = 'Aerotecnico';
