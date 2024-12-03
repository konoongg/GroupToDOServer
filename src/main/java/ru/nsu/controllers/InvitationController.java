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
import ru.nsu.db.services.InvitationService;
import ru.nsu.db.services.UsersInGroupService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Invitation;
import ru.nsu.db.tables.Users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invitation")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersInGroupService usersInGroupService;

    @Autowired
    private GroupsService groupsService;

    @PostMapping("/create/{userId}/{groupId}")
    @Operation(summary = "Create a new invitation", description = "Creates a new invitation for a user to join a group" +
            "body {\n" +
            "  \"userTo\": {\n" +
            "    \"id\": {id}\n" +
            "  },\n" +
            "  \"group\": {\n" +
            "    \"id\": {id}\n" +
            "  }\n" +
            "}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation created successfully"),
            @ApiResponse(responseCode = "403", description = "User is not an admin of the group"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createInvitation(@PathVariable Long userId, @PathVariable Long groupId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users userFrom = usersService.findByLogin(username);
        if (userFrom == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserAdminInGroup(userFrom.getId(), groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "User is not an admin of the group");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (usersInGroupService.isUserInGroup(userId, groupId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "User already is a member of the group");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Invitation invitation = new Invitation();
        invitation.setUserFrom(userFrom);
        invitation.setUserTo(usersService.findById(userId));
        invitation.setGroup(groupsService.findById(groupId));
        invitation.setStatus("PENDING");
        Invitation createdInvitation = invitationService.createInvitation(invitation);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Invitation created successfully");
        response.put("invitationId", createdInvitation.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/accept/{invitationId}")
    @Operation(summary = "Accept an invitation", description = "Accepts an invitation to join a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to accept this invitation"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> acceptInvitation(@PathVariable Long invitationId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Invitation invitation = invitationService.findById(invitationId);
        if (invitation == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Invitation not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!invitation.getUserTo().getId().equals(currentUser.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "You are not authorized to accept this invitation");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Invitation is not pending");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        usersInGroupService.addUserToGroup(currentUser.getId(), invitation.getGroup().getId());

        invitation.setStatus("ACCEPTED");
        invitationService.updateInvitation(invitation);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Invitation accepted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/sent")
    @Operation(summary = "Get sent invitations", description = "Retrieves a list of invitations sent by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getSentInvitations() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Invitation> sentInvitations = invitationService.findByUserFrom(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Invitations retrieved successfully");
        response.put("invitations", sentInvitations);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/received")
    @Operation(summary = "Get received invitations", description = "Retrieves a list of invitations received by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getReceivedInvitations() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        List<Invitation> receivedInvitations = invitationService.findByUserTo(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Invitations retrieved successfully");
        response.put("invitations", receivedInvitations);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{invitationId}")
    @Operation(summary = "Delete an invitation", description = "Deletes an invitation sent by the current user or received by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation deleted successfully"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to delete this invitation"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> deleteInvitation(@PathVariable Long invitationId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Invitation invitation = invitationService.findById(invitationId);
        if (invitation == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Invitation not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!invitation.getUserFrom().getId().equals(currentUser.getId()) && !invitation.getUserTo().getId().equals(currentUser.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("message", "You are not authorized to delete this invitation");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        invitationService.deleteInvitation(invitationId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Invitation deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
