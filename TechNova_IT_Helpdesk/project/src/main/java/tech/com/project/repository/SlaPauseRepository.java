package tech.com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.com.project.model.SlaPause;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaPauseRepository extends JpaRepository<SlaPause, Integer> {

    Optional<SlaPause> findFirstByTicketIdAndResumedAtIsNull(Integer ticketId);

    @Query("SELECT p FROM SlaPause p JOIN FETCH p.pausedBy LEFT JOIN FETCH p.resumedBy WHERE p.ticket.id = :ticketId ORDER BY p.pausedAt DESC")
    List<SlaPause> findByTicketIdWithUsers(@Param("ticketId") Integer ticketId);

    List<SlaPause> findByTicketIdAndResumedAtIsNotNull(Integer ticketId);
}
