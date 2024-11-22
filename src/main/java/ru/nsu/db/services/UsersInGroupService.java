package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.db.repositoris.UsersInGroupRepository;
import ru.nsu.db.tables.UsersInGroup;

@Service
public class UsersInGroupService {

    @Autowired
    private UsersInGroupRepository usersInGroupRepository;

    public boolean isUserInGroup(Long userId, Long groupId) {
        return usersInGroupRepository.existsByUserIdAndGroupId(userId, groupId);
    }

    public boolean isUserAdminInGroup(Long userId, Long groupId) {
        UsersInGroup usersInGroup = usersInGroupRepository.findByUserIdAndGroupId(userId, groupId);
        return usersInGroup != null && usersInGroup.isAdmin();
    }
}
