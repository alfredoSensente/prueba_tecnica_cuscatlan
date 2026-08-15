package com.cuscatlan.reserva.repository;

import com.cuscatlan.reserva.entity.Space;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    List<Space> findByActiveTrue();
}
