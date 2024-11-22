package ru.nsu.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.nsu.db.services.JwtTokenService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Groups;
import ru.nsu.db.tables.Users;
import ru.nsu.db.tables.dto.LoginRequest;
import ru.nsu.db.tables.dto.UpdatePasswordRequest;
import ru.nsu.exceptions.UserAlreadyExistsException;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @DeleteMapping("/delete")
    @Operation(summary = "Delete user account", description = "Deletes the current user's account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteAccount(@RequestBody LoginRequest loginRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        userService.deleteUser(user);
        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/updatePassword")
    @Operation(summary = "Update user password", description = "Updates the current user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid old password")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), userDetails.getPassword())) {
            return new ResponseEntity<>("Invalid old password", HttpStatus.BAD_REQUEST);
        }
        boolean success = userService.updatePassword(username, updatePasswordRequest.getNewPassword());
        if (success) {
            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid old password", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateLogin")
    @Operation(summary = "Update user login", description = "Updates the current user's login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login updated successfully"),
            @ApiResponse(responseCode = "400", description = "Login already exists")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateLogin(@RequestParam String newLogin) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        boolean success = userService.updateLogin(username, newLogin);
        if (success) {
            return new ResponseEntity<>("Login updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Login already exists", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateEmail")
    @Operation(summary = "Update user email", description = "Updates the current user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully"),
            @ApiResponse(responseCode = "400", description = "Email already exists")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateEmail(@RequestParam String newEmail) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        boolean success = userService.updateEmail(username, newEmail);
        if (success) {
            return new ResponseEntity<>("Email updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/groups")
    @Operation(summary = "Get user groups", description = "Retrieves a list of groups the current user is a member of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Groups>> getUserGroups() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        List<Groups> groups = userService.getUserGroups(username);
        return ResponseEntity.ok(groups);
    }
}



