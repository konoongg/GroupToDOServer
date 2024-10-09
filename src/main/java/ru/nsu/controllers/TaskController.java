package ru.nsu.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.db.services.TasksService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Tasks;
import ru.nsu.db.tables.Users;
import ru.nsu.exceptions.UserAlreadyExistsException;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TasksService taskService;

    @Autowired
    private UsersService userService;
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Tasks> createTask(@RequestBody Tasks task) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        task.setOwner(user);
        Tasks createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }
}
