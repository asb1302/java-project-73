package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.TaskStatusDto;
import hexlet.code.javaproject73.model.TaskStatus;

public interface TaskStatusService {
    TaskStatus createTaskStatus(TaskStatusDto taskStatusDto);
    TaskStatus updateTaskStatus(TaskStatusDto taskStatusDto, long id);
}
