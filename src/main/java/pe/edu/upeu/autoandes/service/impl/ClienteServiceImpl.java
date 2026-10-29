package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.service.ClienteService;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.mapper.ClienteMapper;
import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository repository;
    private final ClienteMapper mapper;
    public List<ClienteResponseDTO> listar() { return repository.findAllByOrderByNombresAsc().stream().map(mapper::toResponse).toList(); }
}
