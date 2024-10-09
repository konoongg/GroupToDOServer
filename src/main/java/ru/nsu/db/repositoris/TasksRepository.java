package ru.nsu.db.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.db.tables.Tasks;
import ru.nsu.db.tables.Users;

public interface TasksRepository extends JpaRepository<Tasks, Long> {

}



