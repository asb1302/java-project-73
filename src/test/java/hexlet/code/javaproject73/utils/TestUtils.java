package hexlet.code.javaproject73.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.javaproject73.component.JWTHelper;
import hexlet.code.javaproject73.dto.TaskStatusDto;
import hexlet.code.javaproject73.dto.UserDto;
import hexlet.code.javaproject73.model.User;
import hexlet.code.javaproject73.repository.TaskStatusRepository;
import hexlet.code.javaproject73.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static hexlet.code.javaproject73.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.javaproject73.controller.UserController.USER_CONTROLLER_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TestUtils {
    public static final String BASE_URL = "/api";
    public static final String TEST_USERNAME = "email@email.com";
    public static final String TEST_USERNAME_2 = "email2@email.com";

    public static final String TEST_STATUS_NAME = "status";
    public static final String TEST_STATUS_NAME_2 = "status_2";

    private final UserDto testRegistrationDto = new UserDto(
            TEST_USERNAME,
            "fname",
            "lname",
            "pwd"
    );

    private final TaskStatusDto testStatusDto = new TaskStatusDto(
            TEST_STATUS_NAME
    );

    public UserDto getTestRegistrationDto() {
        return testRegistrationDto;
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private JWTHelper jwtHelper;

    public void tearDown() {
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    public User getUserByEmail(final String email) {
        return userRepository.findByEmail(email).get();
    }

    public ResultActions regDefaultUser() throws Exception {
        return regUser(testRegistrationDto);
    }

    public ResultActions regDefaultStatus(final String byUser) throws Exception {
        return regStatus(testStatusDto, byUser);
    }

    public ResultActions regUser(final UserDto dto) throws Exception {
        final var request = MockMvcRequestBuilders.post(BASE_URL + USER_CONTROLLER_PATH)
                .content(asJson(dto))
                .contentType(APPLICATION_JSON);

        return perform(request);
    }

    public ResultActions regStatus(final TaskStatusDto dto, final String byUser) throws Exception {
        final var request = post(BASE_URL + TASK_STATUS_CONTROLLER_PATH)
                .content(asJson(dto))
                .contentType(APPLICATION_JSON);

        return perform(request, byUser);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request, final String byUser) throws Exception {
        final String token = jwtHelper.expiring(Map.of("username", byUser));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }
}
