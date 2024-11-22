package ru.nsu.db.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.db.tables.Invitation;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByUserFromId(Long userId);
    List<Invitation> findByUserToId(Long userId);
}