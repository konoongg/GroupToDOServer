package ru.nsu.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Operation(summary = "Create a new group", description = "Creates a new group with the specified name " +
            "body {\n" +
            "    \"groupName\": \"New Group\"\n" +
            "}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody CreateGroupRequest createGroupRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Groups group = groupService.createGroup(createGroupRequest.getGroupName(), user);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Group created successfully");
        response.put("groupId", group.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{groupId}")
    @Operation(summary = "Delete a group", description = "Deletes the group with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User is not an admin of the group"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable Long groupId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserInGroup(user.getId(), groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "User is not a member of the group");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (!usersInGroupService.isUserAdminInGroup(user.getId(), groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "User is not an admin of the group");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        groupService.deleteGroup(groupId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Group deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/removeUser/{groupId}/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remove a user from a group", description = "Removes the specified user from the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
            @ApiResponse(responseCode = "403", description = "User is not an admin of the group"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserAdminInGroup(currentUser.getId(), groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "User is not an admin of the group");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        Users userToRemove = usersService.findById(userId);
        if (userToRemove == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User to remove not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserInGroup(userToRemove.getId(), groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "User is not a member of the group");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        usersInGroupService.removeUserFromGroup(userToRemove.getId(), groupId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "User removed from group successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/members/{groupId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get group members", description = "Get the list of members in the group and their roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group members retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable Long groupId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserInGroup(currentUser.getId(), groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "User is not a member of the group");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        Groups group = groupService.findById(groupId);
        if (group == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Group not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Map<String, Object>> members = group.getUsersInGroup().stream()
                .map(userInGroup -> {
                    Map<String, Object> member = new HashMap<>();
                    member.put("userId", userInGroup.getUser().getId());
                    member.put("username", userInGroup.getUser().getLogin());
                    member.put("isAdmin", userInGroup.isAdmin());
                    return member;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Group members retrieved successfully");
        response.put("members", members);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
