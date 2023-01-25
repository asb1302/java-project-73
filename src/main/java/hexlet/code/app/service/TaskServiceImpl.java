package hexlet.code.app.service;

import hexlet.code.app.dto.TaskDto;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        final Set<Label> labels = Optional.ofNullable(dto.getLabelIds())
                .orElse(Set.of())
                .stream()
                .filter(Objects::nonNull)
                .map(Label::new)
                .collect(Collectors.toSet());

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(taskStatus)
                .name(dto.getName())
                .description(dto.getDescription())
                .labels(labels)
                .build();
    }
}
