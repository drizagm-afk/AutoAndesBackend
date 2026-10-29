package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.service.ReporteService;
import pe.edu.upeu.autoandes.service.support.RangoFechas;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private final DetalleOrdenRepository detalles;

    @Override
    public List<IngresoPorServicioDTO> ingresos(LocalDate desde, LocalDate hasta) {
        var rango = RangoFechas.of(desde, hasta);

        return detalles.ingresos(rango.desde(), rango.hastaExclusivo());
    }
}
