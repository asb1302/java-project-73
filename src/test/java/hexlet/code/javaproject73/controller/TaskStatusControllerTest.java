package hexlet.code.javaproject73.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import hexlet.code.javaproject73.config.SpringConfig;
import hexlet.code.javaproject73.dto.TaskStatusDto;
import hexlet.code.javaproject73.model.TaskStatus;
import hexlet.code.javaproject73.repository.TaskStatusRepository;
import hexlet.code.javaproject73.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.javaproject73.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.javaproject73.controller.UserController.ID;
import static hexlet.code.javaproject73.utils.TestUtils.asJson;
import static hexlet.code.javaproject73.utils.TestUtils.TEST_USERNAME_2;
import static hexlet.code.javaproject73.utils.TestUtils.BASE_URL;
import static hexlet.code.javaproject73.utils.TestUtils.TEST_USERNAME;
import static hexlet.code.javaproject73.utils.TestUtils.TEST_STATUS_NAME;
import static hexlet.code.javaproject73.utils.TestUtils.TEST_STATUS_NAME_2;
import static hexlet.code.javaproject73.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(SpringConfig.TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfig.class)
@DBRider
@DataSet("task_statuses.yml")
public class TaskStatusControllerTest {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestUtils utils;

    @Test
    public void registration() throws Exception {
        assertEquals(2, taskStatusRepository.count());
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isCreated());
        assertEquals(3, taskStatusRepository.count());
    }

    @Test
    public void deleteStatus() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME);
        final Long statusId = taskStatusRepository.findAll().get(0).getId();
        assertEquals(3, taskStatusRepository.count());

        utils.perform(delete(BASE_URL + TASK_STATUS_CONTROLLER_PATH + ID, statusId), TEST_USERNAME)
                .andExpect(status().isOk());

        assertEquals(2, taskStatusRepository.count());
    }

    @Test
    public void getTaskStatusById() throws Exception {
        utils.regDefaultUser();
        assertEquals(2, taskStatusRepository.count());

        final TaskStatus expectedStatus = taskStatusRepository.findAll().get(0);

        final var response = utils
                .perform(get(BASE_URL + TASK_STATUS_CONTROLLER_PATH + ID, expectedStatus.getId()), TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        final TaskStatus status = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertEquals(expectedStatus.getId(), status.getId());
        assertEquals(expectedStatus.getName(), status.getName());
    }

    @Test
    public void getAllTaskStatuses() throws Exception {
        utils.regDefaultUser();
        final var response = utils.perform(
                        get(BASE_URL + TASK_STATUS_CONTROLLER_PATH), TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<TaskStatus> taskStatuses = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(taskStatuses).hasSize(2);
        assertThat(response.getContentAsString())
                .contains("status_1", "2023-01-01T00:00:01.000+00:00");
        assertThat(response.getContentAsString())
                .contains("status_2", "2023-01-02T00:00:01.000+00:00");
    }

    @Test
    public void updateStatusById() throws Exception {
        taskStatusRepository.deleteAll();

        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME);

        final long statusID = taskStatusRepository.findAll().get(0).getId();

        TaskStatusDto taskStatusDto = new TaskStatusDto(TEST_STATUS_NAME_2);

        final var updateRequest =
                put(BASE_URL + TASK_STATUS_CONTROLLER_PATH + ID, statusID)
                        .content(asJson(taskStatusDto))
                        .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TEST_USERNAME_2).andExpect(status().isOk());

        assertTrue(taskStatusRepository.existsById(statusID));
        assertTrue(taskStatusRepository.findByName(TEST_STATUS_NAME).isEmpty());
        assertTrue(taskStatusRepository.findByName(TEST_STATUS_NAME_2).isPresent());
    }

    @Test
    public void getStatusByIdFails() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME);
        final TaskStatus expectedStatus = taskStatusRepository.findAll().get(0);

        Exception exception = assertThrows(
                Exception.class, () -> utils.perform(get(BASE_URL + TASK_STATUS_CONTROLLER_PATH + ID,
                        expectedStatus.getId()))
        );
        String message = exception.getMessage();
        assertTrue(message.contains("No value present"));
    }

    @Test
    public void twiceRegTheSameStatusFail() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isCreated());
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isUnprocessableEntity());

        assertEquals(3, taskStatusRepository.count());
    }
}
