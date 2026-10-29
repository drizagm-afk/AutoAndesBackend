package pe.edu.upeu.autoandes.security;

import pe.edu.upeu.autoandes.entity.Rol;
import java.util.*;

public final class PermisosPorRol {
    private static final Map<String,Set<Permiso>> MATRIZ=Map.of(
        "ADMIN", Collections.unmodifiableSet(EnumSet.allOf(Permiso.class)),
        "ASESOR", Set.of(Permiso.SERVICIO_LEER,Permiso.VEHICULO_LEER,Permiso.VEHICULO_ESCRIBIR,
            Permiso.ORDEN_LEER,Permiso.ORDEN_REGISTRAR),
        "JEFE_TALLER", Set.of(Permiso.SERVICIO_LEER,Permiso.SERVICIO_ESCRIBIR,Permiso.VEHICULO_LEER,
            Permiso.ORDEN_LEER,Permiso.ORDEN_ANULAR,Permiso.REPORTE_VER));
    private PermisosPorRol() {}
    public static List<String> permisos(Collection<Rol> roles) {
        Set<String> union=new TreeSet<>();
        roles.forEach(r -> MATRIZ.getOrDefault(r.getNombre(),Set.of()).forEach(p -> union.add(p.name())));
        return List.copyOf(union);
    }
}
