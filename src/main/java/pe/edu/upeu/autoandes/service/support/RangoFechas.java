package pe.edu.upeu.autoandes.service.support;

import pe.edu.upeu.autoandes.exception.DatosInvalidosException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RangoFechas(LocalDateTime desde, LocalDateTime hastaExclusivo) {
    public static RangoFechas of(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DatosInvalidosException("La fecha desde no puede ser posterior a hasta");
        }
        if (hasta != null && hasta.equals(LocalDate.MAX)) {
            throw new DatosInvalidosException("Fecha hasta fuera de rango");
        }

        return new RangoFechas(
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay()
        );
    }
}
