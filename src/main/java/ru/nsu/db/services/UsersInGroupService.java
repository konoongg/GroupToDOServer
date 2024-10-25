package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.db.repositoris.UsersInGroupRepository;
@Service
public class UsersInGroupService {

    @Autowired
    private UsersInGroupRepository usersInGroupRepository;

    public boolean isUserInGroup(Long userId, Long groupId) {
        return usersInGroupRepository.existsByUserIdAndGroupId(userId, groupId);
    }
}
