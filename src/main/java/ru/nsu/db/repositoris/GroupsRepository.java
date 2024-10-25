    package ru.nsu.db.repositoris;

    import org.springframework.data.jpa.repository.JpaRepository;
    import ru.nsu.db.tables.Groups;

    public interface GroupsRepository  extends JpaRepository<Groups, Long> {
    }
