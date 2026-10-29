package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO;
import java.time.LocalDate;
import java.util.List;
public interface ReporteService { List<IngresoPorServicioDTO> ingresos(LocalDate desde,LocalDate hasta); }
