-- Required exam accounts. Passwords are BCrypt hashes of the documented test password.
INSERT INTO roles(nombre) VALUES ('ADMIN');
INSERT INTO roles(nombre) VALUES ('ASESOR');
INSERT INTO roles(nombre) VALUES ('JEFE_TALLER');
INSERT INTO usuarios(username,password,nombrecompleto,activo) VALUES ('admin','$2a$10$LpWQAxYLuTIYR42X75F9PeabUTuHFIS7hu7h7t.DOX9aA7i/DwbqK','Administrador AutoAndes',1);
INSERT INTO usuario_roles(usuarioid,rolid) SELECT u.id,r.id FROM usuarios u CROSS JOIN roles r WHERE u.username='admin' AND r.nombre='ADMIN';
INSERT INTO usuarios(username,password,nombrecompleto,activo) VALUES ('asesor','$2a$10$ZSsxQgxh4reCwm90YRyhxeP2T78A6AxZ52KTE02anMAHN8g7jicGi','Asesor AutoAndes',1);
INSERT INTO usuario_roles(usuarioid,rolid) SELECT u.id,r.id FROM usuarios u CROSS JOIN roles r WHERE u.username='asesor' AND r.nombre='ASESOR';
INSERT INTO usuarios(username,password,nombrecompleto,activo) VALUES ('jefe','$2a$10$F.SuR8DJrx32NW5YF/2yI.Lby9bKiEQjuo1BOauLAo8ljJLfr9yl6','Jefe de taller AutoAndes',1);
INSERT INTO usuario_roles(usuarioid,rolid) SELECT u.id,r.id FROM usuarios u CROSS JOIN roles r WHERE u.username='jefe' AND r.nombre='JEFE_TALLER';
INSERT INTO usuarios(username,password,nombrecompleto,activo) VALUES ('inactivo','$2a$10$Gu1Kz5tkVmlYvktaUV0ZX.jkQZ2Q3AOxbCdcni5cLbn8sO6m3RKEq','Asesor inactivo',0);
INSERT INTO usuario_roles(usuarioid,rolid) SELECT u.id,r.id FROM usuarios u CROSS JOIN roles r WHERE u.username='inactivo' AND r.nombre='ASESOR';
