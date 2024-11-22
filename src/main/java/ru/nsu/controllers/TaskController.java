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
import ru.nsu.db.services.TasksService;
import ru.nsu.db.services.UsersInGroupService;
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

    @Autowired
    private UsersInGroupService usersInGroupService;

    @PostMapping("/create")
    @Operation(summary = "Create a new task", description = "Creates a new task for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task created successfully")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createTask(@RequestBody Tasks task) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        task.setOwner(user);
        Tasks createdTask = taskService.createTask(task);
        return new ResponseEntity<>("task created", HttpStatus.OK);
    }

    @GetMapping("/uncompleted")
    @Operation(summary = "Get uncompleted tasks", description = "Retrieves a list of uncompleted tasks for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uncompleted tasks retrieved successfully")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Tasks>> getUncompletedTasks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        List<Tasks> uncompletedTasks = taskService.findByOwnerIdAndStatus(user.getId(), 0);
        return ResponseEntity.ok(uncompletedTasks);
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed tasks", description = "Retrieves a list of completed tasks for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed tasks retrieved successfully")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Tasks>> getCompletedTasks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);
        List<Tasks> completedTasks = taskService.findByOwnerIdAndStatus(user.getId(), 1);
        return ResponseEntity.ok(completedTasks);
    }

    @PutMapping("/update/{taskId}")
    @Operation(summary = "Update a task", description = "Updates an existing task for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateTask(@PathVariable Long taskId, @RequestBody Tasks updatedTask) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);

        Tasks existingTask = taskService.findById(taskId).orElse(null);
        if (existingTask == null || !existingTask.getOwner().getId().equals(user.getId())) {
            return new ResponseEntity<>("task not found", HttpStatus.NOT_FOUND);
        }
        Tasks updated = taskService.updateTask(taskId, updatedTask);
        if (updated != null) {
            return new ResponseEntity<>("task update", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("task not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{taskId}")
    @Operation(summary = "Delete a task", description = "Deletes an existing task for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);

        Tasks existingTask = taskService.findById(taskId).orElse(null);
        if (existingTask == null || !existingTask.getOwner().getId().equals(user.getId())) {
            return new ResponseEntity<>("task not found", HttpStatus.NOT_FOUND);
        }

        boolean deleted = taskService.deleteTask(taskId);
        if (deleted) {
            return new ResponseEntity<>("task deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("task not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/group/uncompleted/{groupId}")
    @Operation(summary = "Get completed tasks for a group", description = "Retrieves a list of completed tasks for a specific group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Tasks>> getGroupUncompletedTasks(@PathVariable Long groupId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);

        if (!usersInGroupService.isUserInGroup(user.getId(), groupId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Tasks> uncompletedTasks = taskService.findByGroupIdAndStatus(groupId, 0);
        return ResponseEntity.ok(uncompletedTasks);
    }

    @GetMapping("/group/completed/{groupId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Tasks>> getGroupCompletedTasks(@PathVariable Long groupId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = userService.findByLogin(username);

        if (!usersInGroupService.isUserInGroup(user.getId(), groupId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Tasks> uncompletedTasks = taskService.findByGroupIdAndStatus(groupId, 1);
        return ResponseEntity.ok(uncompletedTasks);
    }
}
