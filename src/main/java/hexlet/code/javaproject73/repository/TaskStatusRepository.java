package hexlet.code.javaproject73.repository;

import hexlet.code.javaproject73.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long>  {
    Optional<TaskStatus> findByName(String str);

}
