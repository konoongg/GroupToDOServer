package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.db.repositoris.TasksRepository;
import ru.nsu.db.tables.Tasks;

@Service
public class TasksService {

    @Autowired
    private TasksRepository tasksRepository;

    public Tasks createTask(Tasks task) {
        return tasksRepository.save(task);
    }
}
