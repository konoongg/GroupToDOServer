package ru.nsu.controllers;

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
import ru.nsu.db.tables.Users;
import ru.nsu.db.tables.dto.LoginRequest;
import ru.nsu.db.tables.dto.UpdatePasswordRequest;
import ru.nsu.exceptions.UserAlreadyExistsException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersService userService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @DeleteMapping("/delete")
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
}



