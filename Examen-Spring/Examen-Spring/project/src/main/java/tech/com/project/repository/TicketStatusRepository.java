package tech.com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.com.project.model.TicketStatus;

import java.lang.ScopedValue;
import java.util.Optional;

@Repository
public interface TicketStatusRepository extends JpaRepository<TicketStatus, Integer> {
    Optional<TicketStatus> findByName(String name);
    boolean existsByName(String name);

    <T> ScopedValue<T> findBySystemName(String resuelta);
}
