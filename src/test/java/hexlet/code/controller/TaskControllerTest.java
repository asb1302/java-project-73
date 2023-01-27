package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfig;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.controller.TaskController.ID;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;

@AutoConfigureMockMvc
@ActiveProfiles(SpringConfig.TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfig.class)
public class TaskControllerTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestUtils utils;


    @BeforeEach
    public void clear() {
        utils.tearDown();
    }


    @Test
    public void registration() throws Exception {
        assertEquals(0, taskRepository.count());
        utils.regDefaultTask(TEST_USERNAME).andExpect(status().isCreated());
        assertEquals(1, taskRepository.count());
    }

    @Test
    public void getTaskById() throws Exception {
        utils.regDefaultTask(TEST_USERNAME);
        final var expectedTask = taskRepository.findAll().get(0);

        var response
                = utils
                .perform(get(BASE_URL + TASK_CONTROLLER_PATH + ID, expectedTask.getId()), TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        final Task task = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertEquals(expectedTask.getId(), task.getId());
        assertEquals(expectedTask.getName(), task.getName());
        assertEquals(expectedTask.getDescription(), task.getDescription());

    }

    @Test
    public void getAllTasks() throws Exception {
        utils.regDefaultTask(TEST_USERNAME);
        final var response = utils.perform(get(BASE_URL + TASK_CONTROLLER_PATH),
                        TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(tasks).hasSize(1);
    }

    @Test
    public void updateTask() throws Exception {
        utils.regDefaultTask(TEST_USERNAME);
        Task task = taskRepository.findAll().get(0);
        final Long taskId = task.getId();
        final var newTaskDto = new TaskDto(
                "newTask",
                "newDescription",
                task.getTaskStatus().getId(),
                task.getExecutor().getId(),
                null
        );

        final var updateRequest = put(BASE_URL + TASK_CONTROLLER_PATH + ID,
                taskId)
                .content(asJson(newTaskDto))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TEST_USERNAME).andExpect(status().isOk());
        assertTrue(taskRepository.existsById(taskId));
        assertNull(taskRepository.findByName(task.getName()).orElse(null));
        assertNotNull(taskRepository.findByName(newTaskDto.getName()).orElse(null));
    }

    @Test
    public void deleteTask() throws Exception {
        assertEquals(0, taskRepository.count());
        utils.regDefaultTask(TEST_USERNAME);
        assertEquals(1, taskRepository.count());
        final Long taskID = taskRepository.findAll().get(0).getId();

        utils.perform(delete(BASE_URL + TASK_CONTROLLER_PATH + ID, taskID), TEST_USERNAME)
                .andExpect(status().isOk());
        assertEquals(0, taskRepository.count());
    }

    @Test
    public void getTaskByIdFails() throws Exception {
        utils.regDefaultTask(TEST_USERNAME);
        final Task expectedTask = taskRepository.findAll().get(0);
        utils.perform(get(BASE_URL + TASK_CONTROLLER_PATH + UserController.ID,
                expectedTask.getId())).andExpect(status().isForbidden());
    }

    @Test
    public void regSameTaskTwiceFails() throws Exception {
        utils.regDefaultTask(TEST_USERNAME).andExpect(status().isCreated());
        utils.regDefaultTask(TEST_USERNAME).andExpect(status().isUnprocessableEntity());
        assertEquals(1, taskRepository.count());
    }

    @Test
    public void deleteTaskFails() throws Exception {
        utils.regDefaultTask(TEST_USERNAME);
        final Long taskId = taskRepository.findAll().get(0).getId() + 1;

        utils.perform(delete(BASE_URL + TASK_CONTROLLER_PATH + UserController.ID, taskId), TEST_USERNAME)
                .andExpect(status().isNotFound());
        assertEquals(1, taskRepository.count());
    }
}
