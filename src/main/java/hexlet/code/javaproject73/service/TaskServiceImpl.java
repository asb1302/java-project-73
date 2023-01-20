package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.TaskDto;
import hexlet.code.javaproject73.model.Task;
import hexlet.code.javaproject73.model.TaskStatus;
import hexlet.code.javaproject73.model.User;
import hexlet.code.javaproject73.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private TaskRepository taskRepository;
    private final UserService userService;

    @Override
    public Task createNewTask(TaskDto taskDto) {
        final Task newTask = fromDto(taskDto);
        return taskRepository.save(newTask);
    }

    @Override
    public Task updateTask(TaskDto taskDto, long id) {
        final Task taskToUpdate = fromDto(taskDto);
        taskToUpdate.setId(id);
        return taskRepository.save(taskToUpdate);
    }

    private Task fromDto(final TaskDto dto) {
        final User author = userService.getCurrentUser();
        final User executor = Optional.ofNullable(dto.getExecutorId())
                .map(User::new)
                .orElse(null);
        final TaskStatus taskStatus = Optional.ofNullable(dto.getTaskStatusId())
                .map(TaskStatus::new)
                .orElse(null);

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(taskStatus)
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
