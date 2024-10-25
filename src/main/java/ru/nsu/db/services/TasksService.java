package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.db.repositoris.TasksRepository;
import ru.nsu.db.tables.Tasks;

import java.util.List;
import java.util.Optional;

@Service
public class TasksService {

    @Autowired
    private TasksRepository tasksRepository;

    public Tasks createTask(Tasks task) {
        return tasksRepository.save(task);
    }

    public List<Tasks> findByOwnerIdAndStatus(Long ownerId, int status) {
        return tasksRepository.findByOwnerIdAndStatus(ownerId, status);
    }

    public Optional<Tasks> findById(Long taskId) {
        return tasksRepository.findById(taskId);
    }

    public Tasks updateTask(Long taskId, Tasks updatedTask) {
        Optional<Tasks> existingTask = tasksRepository.findById(taskId);
        if (existingTask.isPresent()) {
            Tasks task = existingTask.get();
            task.setName(updatedTask.getName());
            task.setDes(updatedTask.getDes());
            task.setLocation(updatedTask.getLocation());
            task.setGroupId(updatedTask.getGroupId());
            task.setStatus(updatedTask.getStatus());
            task.setType(updatedTask.getType());
            task.setDate(updatedTask.getDate());
            return tasksRepository.save(task);
        } else {
            return null;
        }
    }

    public boolean deleteTask(Long taskId) {
        Optional<Tasks> existingTask = tasksRepository.findById(taskId);
        if (existingTask.isPresent()) {
            tasksRepository.deleteById(taskId);
            return true;
        } else {
            return false;
        }
    }
}
