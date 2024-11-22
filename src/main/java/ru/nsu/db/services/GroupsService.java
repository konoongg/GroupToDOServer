package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.db.repositoris.GroupsRepository;
import ru.nsu.db.repositoris.UsersInGroupRepository;
import ru.nsu.db.repositoris.UsersRepository;
import ru.nsu.db.tables.Groups;
import ru.nsu.db.tables.Users;
import ru.nsu.db.tables.UsersInGroup;

@Service
public class GroupsService {
    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersInGroupRepository usersInGroupRepository;

    @Transactional
    public Groups createGroup(String groupName, Users user) {
        Groups group = new Groups();
        group.setName(groupName);
        group = groupsRepository.save(group);

        UsersInGroup usersInGroup = new UsersInGroup();
        usersInGroup.setUser(user);
        usersInGroup.setGroup(group);
        usersInGroup.setAdmin(true);
        usersInGroupRepository.save(usersInGroup);
        return group;
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        usersInGroupRepository.deleteByGroupId(groupId);
        groupsRepository.deleteById(groupId);
    }
}
