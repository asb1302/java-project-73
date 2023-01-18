package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.TaskStatusDto;
import hexlet.code.javaproject73.model.TaskStatus;
import hexlet.code.javaproject73.repository.TaskStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@AllArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {
    private TaskStatusRepository taskStatusRepository;

    @Override
    public TaskStatus createTaskStatus(final TaskStatusDto dto) {
        final TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(dto.getName());
        return taskStatusRepository.save(taskStatus);
    }

    @Override
    public TaskStatus updateTaskStatus(TaskStatusDto taskStatusDto, long id) {
        TaskStatus statusDto = taskStatusRepository.findById(id).get();
        statusDto.setName(taskStatusDto.getName());
        return taskStatusRepository.save(statusDto);
    }
}
