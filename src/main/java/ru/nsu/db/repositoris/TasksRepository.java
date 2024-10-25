package ru.nsu.db.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.db.tables.Tasks;
import ru.nsu.db.tables.Users;

import java.util.List;

public interface TasksRepository extends JpaRepository<Tasks, Long> {
    List<Tasks> findByOwnerIdAndStatus(Long ownerId, int status);
    List<Tasks> findByGroupIdAndStatus(Long groupId, int status);
}



