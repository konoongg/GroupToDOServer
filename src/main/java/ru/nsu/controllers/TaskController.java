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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TasksService taskService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersInGroupService usersInGroupService;

    @PostMapping("/create")
    @Operation(summary = "Create a new task", description = "Creates a new task for the current user " +
            "body{\n" +
            "    \"name\": \"New Task\",\n" +
            "    \"des\": \"This is a new task\",\n" +
            "    \"location\": \"Some location\",\n" +
            "    \"groupId\": 1,\n" +
            "    \"status\": 0,\n" +
            "    \"type\": 1,\n" +
            "    \"date\": \"2023-12-31T23:59:59\"\n" +
            "}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Tasks task) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        task.setOwner(user);
        Tasks createdTask = taskService.createTask(task);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Task created successfully");
        response.put("taskId", createdTask.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/uncompleted")
    @Operation(summary = "Get uncompleted tasks", description = "Retrieves a list of uncompleted tasks for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uncompleted tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getUncompletedTasks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Users user = usersService.findByLogin(username);
        List<Tasks> uncompletedTasks = taskService.findByOwnerIdAndStatus(user.getId(), 0);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Uncompleted tasks retrieved successfully");
        response.put("tasks", uncompletedTasks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed tasks", description = "Retrieves a list of completed tasks for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getCompletedTasks() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users currentUser = usersService.findByLogin(username);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Users user = usersService.findByLogin(username);
        List<Tasks> completedTasks = taskService.findByOwnerIdAndStatus(user.getId(), 1);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Completed tasks retrieved successfully");
        response.put("tasks", completedTasks);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{taskId}")
    @Operation(summary = "Update a task", description = "Updates an existing task for the current user" +
            "body{\n" +
            "    \"name\": \"Updated Task\",\n" +
            "    \"des\": \"This task has been updated\",\n" +
            "    \"location\": \"Updated location\",\n" +
            "    \"groupId\": 1,\n" +
            "    \"status\": 1,\n" +
            "    \"type\": 2,\n" +
            "    \"date\": \"2024-12-31T23:59:59\"\n" +
            "}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable Long taskId, @RequestBody Tasks updatedTask) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Tasks existingTask = taskService.findById(taskId).orElse(null);
        if (existingTask == null || !existingTask.getOwner().getId().equals(user.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Task not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Tasks updated = taskService.updateTask(taskId, updatedTask);
        if (updated != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Task updated successfully");
            response.put("taskId", updated.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Task not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{taskId}")
    @Operation(summary = "Delete a task", description = "Deletes an existing task for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable Long taskId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        Users user = usersService.findByLogin(username);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Tasks existingTask = taskService.findById(taskId).orElse(null);
        if (existingTask == null || !existingTask.getOwner().getId().equals(user.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Task not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        boolean deleted = taskService.deleteTask(taskId);
        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Task deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "Task not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/group/uncompleted/{groupId}")
    @Operation(summary = "Get completed tasks for a group", description = "Retrieves a list of completed tasks for a specific group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getGroupUncompletedTasks(@PathVariable Long groupId) {
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

        List<Tasks> uncompletedTasks = taskService.findByGroupIdAndStatus(groupId, 0);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Uncompleted tasks retrieved successfully");
        response.put("tasks", uncompletedTasks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/completed/{groupId}")
    @Operation(summary = "Get completed tasks for a group", description = "Retrieves a list of completed tasks for a specific group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getGroupCompletedTasks(@PathVariable Long groupId) {
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

        List<Tasks> completedTasks = taskService.findByGroupIdAndStatus(groupId, 1);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Completed tasks retrieved successfully");
        response.put("tasks", completedTasks);
        return ResponseEntity.ok(response);
    }
}
