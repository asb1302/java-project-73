package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfig;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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

import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.TEST_USERNAME_2;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_USERNAME;
import static hexlet.code.utils.TestUtils.TEST_STATUS_NAME;
import static hexlet.code.utils.TestUtils.TEST_STATUS_NAME_2;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class TaskStatusControllerTest {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void registration() throws Exception {
        assertEquals(0, taskStatusRepository.count());
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isCreated());
        assertEquals(1, taskStatusRepository.count());
    }

    @Test
    public void deleteStatus() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME);
        final Long statusId = taskStatusRepository.findAll().get(0).getId();
        assertEquals(1, taskStatusRepository.count());

        utils.perform(delete(BASE_URL + TASK_STATUS_CONTROLLER_PATH + ID, statusId), TEST_USERNAME)
                .andExpect(status().isOk());

        assertEquals(0, taskStatusRepository.count());
    }

    @Test
    public void getTaskStatusById() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isCreated());
        assertEquals(1, taskStatusRepository.count());

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
        utils.regDefaultStatus(TEST_USERNAME);
        final var response = utils.perform(
                        get(BASE_URL + TASK_STATUS_CONTROLLER_PATH), TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<TaskStatus> taskStatuses = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(taskStatuses).hasSize(1);
    }

    @Test
    public void updateStatusById() throws Exception {
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

        utils.perform(get(BASE_URL + TASK_STATUS_CONTROLLER_PATH + ID,
                expectedStatus.getId())).andExpect(status().isForbidden());
    }

    @Test
    public void twiceRegTheSameStatusFail() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isCreated());
        utils.regDefaultStatus(TEST_USERNAME).andExpect(status().isUnprocessableEntity());

        assertEquals(1, taskStatusRepository.count());
    }
}
