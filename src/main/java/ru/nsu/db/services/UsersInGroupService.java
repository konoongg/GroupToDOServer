package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.db.repositoris.UsersInGroupRepository;
import ru.nsu.db.tables.UsersInGroup;

@Service
public class UsersInGroupService {

    @Autowired
    private UsersInGroupRepository usersInGroupRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private GroupsService groupsService;

    public boolean isUserInGroup(Long userId, Long groupId) {
        return usersInGroupRepository.existsByUserIdAndGroupId(userId, groupId);
    }

    public boolean isUserAdminInGroup(Long userId, Long groupId) {
        UsersInGroup usersInGroup = usersInGroupRepository.findByUserIdAndGroupId(userId, groupId);
        return usersInGroup != null && usersInGroup.isAdmin();
    }

    public void addUserToGroup(Long userId, Long groupId) {
        UsersInGroup usersInGroup = new UsersInGroup();
        usersInGroup.setUser(usersService.findById(userId));
        usersInGroup.setGroup(groupsService.findById(groupId));
        usersInGroup.setAdmin(false);
        usersInGroupRepository.save(usersInGroup);
    }
}
