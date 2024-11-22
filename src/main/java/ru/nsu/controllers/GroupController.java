package ru.nsu.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.nsu.db.services.GroupsService;
import ru.nsu.db.services.UsersInGroupService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Groups;
import ru.nsu.db.tables.Users;
import ru.nsu.db.tables.dto.CreateGroupRequest;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupsService groupService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersInGroupService usersInGroupService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupRequest createGroupRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        Groups group = groupService.createGroup(createGroupRequest.getGroupName(), user);
        return new ResponseEntity<>("Group created successfully", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{groupId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserInGroup(user.getId(), groupId)) {
            return new ResponseEntity<>("User is not a member of the group", HttpStatus.FORBIDDEN);
        }

        if (!usersInGroupService.isUserAdminInGroup(user.getId(), groupId)) {
            return new ResponseEntity<>("User is not an admin of the group", HttpStatus.FORBIDDEN);
        }

        groupService.deleteGroup(groupId);
        return new ResponseEntity<>("Group deleted successfully", HttpStatus.OK);
    }

    @DeleteMapping("/removeUser/{groupId}/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserAdminInGroup(currentUser.getId(), groupId)) {
            return new ResponseEntity<>("User is not an admin of the group", HttpStatus.FORBIDDEN);
        }

        Users userToRemove = usersService.findById(userId);
        if (userToRemove == null) {
            return new ResponseEntity<>("User to remove not found", HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserInGroup(userToRemove.getId(), groupId)) {
            return new ResponseEntity<>("User is not a member of the group", HttpStatus.FORBIDDEN);
        }

        usersInGroupService.removeUserFromGroup(userToRemove.getId(), groupId);
        return new ResponseEntity<>("User removed from group successfully", HttpStatus.OK);
    }
}
