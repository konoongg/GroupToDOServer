package ru.nsu.db.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.db.tables.UsersInGroup;

public interface UsersInGroupRepository  extends JpaRepository<UsersInGroup, Long> {
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
}
