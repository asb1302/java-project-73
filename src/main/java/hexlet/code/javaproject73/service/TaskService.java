package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.TaskDto;
import hexlet.code.javaproject73.model.Task;

public interface TaskService {
    Task createNewTask(TaskDto taskDto);

    Task updateTask(TaskDto taskDto, long id);
}
