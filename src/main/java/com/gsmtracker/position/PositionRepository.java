package com.gsmtracker.position;

import com.gsmtracker.device.Device;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    boolean existsByDeviceAndRecordedAt(Device device, Instant recordedAt);

    Optional<Position> findFirstByDeviceOrderByRecordedAtDesc(Device device);

    List<Position> findByDeviceAndRecordedAtBetweenOrderByRecordedAtAsc(
            Device device, Instant from, Instant to, Pageable pageable);
}
