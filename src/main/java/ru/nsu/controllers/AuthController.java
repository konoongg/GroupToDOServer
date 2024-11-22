package ru.nsu.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.db.services.JwtTokenService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Users;
import ru.nsu.db.tables.dto.LoginRequest;
import ru.nsu.exceptions.UserAlreadyExistsException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsersService userService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with the provided credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<String> register(@RequestBody Users appUser) {
        try {
            Users registeredUser = userService.registerUser(appUser);
            String jwt = jwtTokenService.generateToken(registeredUser.getLogin());
            return ResponseEntity.ok(jwt);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user", description = "Authenticates a user with the provided credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or authentication failed")
    })
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenService.generateToken(loginRequest.getLogin());
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }
}



