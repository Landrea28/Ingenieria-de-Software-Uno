INSERT INTO fuerza_publica (codigo, nombre) VALUES
('POLICIA', 'Policia Nacional'),
('EJERCITO', 'Ejercito Nacional'),
('ARMADA', 'Armada Nacional'),
('FUERZA_AEREA', 'Fuerza Aerea Colombiana');

INSERT INTO rango (fuerza_codigo, nombre) VALUES
('POLICIA', 'Patrullero'), ('POLICIA', 'Subintendente'), ('POLICIA', 'Intendente'),
('EJERCITO', 'Soldado'), ('EJERCITO', 'Cabo'), ('EJERCITO', 'Sargento'),
('ARMADA', 'Marinero'), ('ARMADA', 'Cabo Primero'), ('ARMADA', 'Suboficial'),
('FUERZA_AEREA', 'Aerotecnico'), ('FUERZA_AEREA', 'Tecnico Primero'), ('FUERZA_AEREA', 'Suboficial');

INSERT INTO perfil (codigo, nombre) VALUES
('ADMINISTRADOR', 'Administrador'),
('PERSONAL_FUERZA_PUBLICA', 'Personal de fuerza publica');

INSERT INTO permiso (codigo, nombre) VALUES
('CIUDADANOS_LEER', 'Consultar ciudadanos'),
('CIUDADANOS_CREAR', 'Registrar ciudadanos'),
('CIUDADANOS_ACTUALIZAR', 'Actualizar ciudadanos'),
('ANTECEDENTES_CONSULTAR', 'Consultar antecedentes'),
('ANTECEDENTES_ACTUALIZAR', 'Actualizar estados de demostracion'),
('REPORTES_GENERAR', 'Generar reportes'),
('SEGURIDAD_ADMINISTRAR', 'Administrar usuarios y perfiles');

INSERT INTO opcion (codigo, nombre, ruta) VALUES
('CIUDADANOS', 'Gestion de ciudadanos', '/ciudadanos'),
('ANTECEDENTES', 'Consulta de antecedentes', '/antecedentes'),
('REPORTES', 'Reportes de ciudadanos', '/reportes'),
('SEGURIDAD', 'Gestion de seguridad', '/seguridad');
