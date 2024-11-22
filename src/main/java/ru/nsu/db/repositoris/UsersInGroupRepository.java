package ru.nsu.db.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nsu.db.tables.UsersInGroup;

public interface UsersInGroupRepository extends JpaRepository<UsersInGroup, Long> {
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    @Modifying
    @Query("DELETE FROM UsersInGroup uig WHERE uig.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    UsersInGroup findByUserIdAndGroupId(Long userId, Long groupId);
}
