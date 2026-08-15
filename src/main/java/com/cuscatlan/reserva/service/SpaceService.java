package com.cuscatlan.reserva.service;

import com.cuscatlan.reserva.dto.space.SpaceRequest;
import com.cuscatlan.reserva.dto.space.SpaceResponse;
import com.cuscatlan.reserva.entity.Space;
import com.cuscatlan.reserva.exception.SpaceNotFoundException;
import com.cuscatlan.reserva.mapper.SpaceMapper;
import com.cuscatlan.reserva.repository.SpaceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    public SpaceService(SpaceRepository spaceRepository, SpaceMapper spaceMapper) {
        this.spaceRepository = spaceRepository;
        this.spaceMapper = spaceMapper;
    }

    @Transactional
    public SpaceResponse create(SpaceRequest request) {
        Space space = spaceMapper.toEntity(request);
        space.setActive(true);
        spaceRepository.save(space);
        return spaceMapper.toResponse(space);
    }

    @Transactional(readOnly = true)
    public List<SpaceResponse> findAllActive() {
        return spaceRepository.findByActiveTrue().stream()
                .map(spaceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpaceResponse findById(Long id) {
        return spaceMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public SpaceResponse update(Long id, SpaceRequest request) {
        Space space = getOrThrow(id);
        spaceMapper.updateEntityFromRequest(request, space);
        return spaceMapper.toResponse(space);
    }

    @Transactional
    public void delete(Long id) {
        Space space = getOrThrow(id);
        space.setActive(false);
    }

    private Space getOrThrow(Long id) {
        return spaceRepository.findById(id).orElseThrow(() -> new SpaceNotFoundException(id));
    }
}
