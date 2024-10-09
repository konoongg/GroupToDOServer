package ru.nsu.db.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nsu.db.tables.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByLogin(String login);
    Users findByEmail(String email);
}
