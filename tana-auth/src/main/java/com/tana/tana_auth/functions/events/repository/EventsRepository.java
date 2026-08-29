package com.tana.tana_auth.functions.events.repository;

import com.tana.tana_common.model.EventsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventsRepository extends JpaRepository<EventsMaster, Long> {
    Optional<EventsMaster> findByEventSlug(String eventSlug);
}
