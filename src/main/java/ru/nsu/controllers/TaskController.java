package ru.nsu.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.nsu.db.services.TasksService;
import ru.nsu.db.services.UsersService;
import ru.nsu.db.tables.Tasks;
import ru.nsu.db.tables.Users;

import java.util.List;

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

    @GetMapping("/uncompleted")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Tasks>> getUncompletedTasks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        List<Tasks> uncompletedTasks = taskService.findByOwnerIdAndStatus(user.getId(), 0);
        return ResponseEntity.ok(uncompletedTasks);
    }

    @GetMapping("/completed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Tasks>> getCompletedTasks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        List<Tasks> completedTasks = taskService.findByOwnerIdAndStatus(user.getId(), 1);
        return ResponseEntity.ok(completedTasks);
    }

    @PutMapping("/update/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Tasks> updateTask(@PathVariable Long taskId, @RequestBody Tasks updatedTask) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);

        Tasks existingTask = taskService.findById(taskId).orElse(null);
        if (existingTask == null || !existingTask.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Tasks updated = taskService.updateTask(taskId, updatedTask);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);

        Tasks existingTask = taskService.findById(taskId).orElse(null);
        if (existingTask == null || !existingTask.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = taskService.deleteTask(taskId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
