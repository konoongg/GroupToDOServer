package ru.nsu.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.nsu.db.services.InvitationService;
import ru.nsu.db.services.UsersInGroupService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Invitation;
import ru.nsu.db.tables.Users;

import java.util.List;

@RestController
@RequestMapping("/invitation")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersInGroupService usersInGroupService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createInvitation(@RequestBody Invitation invitation) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users userFrom = usersService.findByLogin(username);
        if (userFrom == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (!usersInGroupService.isUserAdminInGroup(userFrom.getId(), invitation.getGroup().getId())) {
            return new ResponseEntity<>("User is not an admin of the group", HttpStatus.FORBIDDEN);
        }

        if (usersInGroupService.isUserInGroup(invitation.getUserTo().getId(), invitation.getGroup().getId())) {
            return new ResponseEntity<>("User already is a member of the group", HttpStatus.OK);
        }

        invitation.setUserFrom(userFrom);
        invitation.setStatus("PENDING");
        invitationService.createInvitation(invitation);
        return new ResponseEntity<>("Invitation created successfully", HttpStatus.OK);
    }

    @PutMapping("/accept/{invitationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> acceptInvitation(@PathVariable Long invitationId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Invitation invitation = invitationService.findById(invitationId);
        if (invitation == null) {
            return new ResponseEntity<>("Invitation not found", HttpStatus.NOT_FOUND);
        }

        if (!invitation.getUserTo().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>("You are not authorized to accept this invitation", HttpStatus.FORBIDDEN);
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            return new ResponseEntity<>("Invitation is not pending", HttpStatus.BAD_REQUEST);
        }


        usersInGroupService.addUserToGroup(currentUser.getId(), invitation.getGroup().getId());

        invitation.setStatus("ACCEPTED");
        invitationService.updateInvitation(invitation);

        return new ResponseEntity<>("Invitation accepted successfully", HttpStatus.OK);
    }

    @GetMapping("/sent")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Invitation>> getSentInvitations() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Invitation> sentInvitations = invitationService.findByUserFrom(currentUser.getId());
        return new ResponseEntity<>(sentInvitations, HttpStatus.OK);
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Invitation>> getReceivedInvitations() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Invitation> receivedInvitations = invitationService.findByUserTo(currentUser.getId());
        return new ResponseEntity<>(receivedInvitations, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{invitationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteInvitation(@PathVariable Long invitationId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Invitation invitation = invitationService.findById(invitationId);
        if (invitation == null) {
            return new ResponseEntity<>("Invitation not found", HttpStatus.NOT_FOUND);
        }

        if (!invitation.getUserFrom().getId().equals(currentUser.getId()) && !invitation.getUserTo().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>("You are not authorized to delete this invitation", HttpStatus.FORBIDDEN);
        }

        invitationService.deleteInvitation(invitationId);
        return new ResponseEntity<>("Invitation deleted successfully", HttpStatus.OK);
    }
}
