package hexlet.code.service;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

    private final TaskStatusRepository taskStatusRepository;

    private final UserRepository userRepository;

    private final LabelRepository labelRepository;

    @Override
    public Task createNewTask(TaskDto taskDto) {
        final Task newTask = fromDto(taskDto);
        return taskRepository.save(newTask);
    }

    @Override
    public Task updateTask(TaskDto taskDto, long id) {
        final Task updateTask = taskRepository.findById(id).get();
        final User author = userService.getCurrentUser();

        updateTask.setName(taskDto.getName());
        updateTask.setAuthor(author);
        updateTask.setExecutor(taskDto.getExecutorId() == null ? null : userRepository
                .findById(taskDto.getExecutorId()).orElse(null));
        updateTask.setTaskStatus(taskStatusRepository.findById(taskDto.getTaskStatusId()).get());
        updateTask.setDescription(taskDto.getDescription());
        updateTask.setLabels(taskDto.getLabelIds() == null ? null : addLabels(taskDto.getLabelIds()));

        return taskRepository.save(updateTask);
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

    private Set<Label> addLabels(Set<Long> labelIds) {
        Set<Label> labels = new HashSet<>();
        for (Long id : labelIds) {
            Label label = labelRepository.findById(id).get();
            labels.add(label);
        }
        return labels;
    }
}
