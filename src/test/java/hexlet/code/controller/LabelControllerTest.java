package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfig;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.utils.TestUtils.BASE_URL;
import static hexlet.code.utils.TestUtils.TEST_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class LabelControllerTest {

    @Autowired
    private TestUtils utils;
    @Autowired
    private LabelRepository labelRepository;

    public static final String LABEL_CONTROLLER_PATH = "/labels";
    public static final String ID = "/{id}";


    @BeforeEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    void registration() throws Exception {
        assertEquals(0, labelRepository.count());
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_USERNAME).andExpect(status().isCreated());
        assertEquals(1, labelRepository.count());
    }

    @Test
    void getById() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_USERNAME);
        final var expectedLabel = labelRepository.findAll().get(0);

        var response =
                utils.perform(get(BASE_URL + LABEL_CONTROLLER_PATH + ID,
                                        expectedLabel.getId()),
                                TEST_USERNAME)
                        .andExpect(status().isOk()).andReturn().getResponse();

        final Label label = TestUtils.fromJson(response.getContentAsString(), new TypeReference<>() {
        });


        Assertions.assertEquals(expectedLabel.getId(), label.getId());
        Assertions.assertEquals(expectedLabel.getName(), label.getName());
    }
    @Test
    void getAll() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TestUtils.TEST_USERNAME);
        utils.regLabel(TestUtils.LABEL_DTO_2, TestUtils.TEST_USERNAME);

        var response =
                utils.perform(get(BASE_URL + LABEL_CONTROLLER_PATH), TestUtils.TEST_USERNAME)
                        .andExpect(status().isOk()).andReturn().getResponse();

        List<Label> list = TestUtils.fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertEquals(2, list.size());
    }

    @Test
    void update() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TestUtils.TEST_USERNAME);

        final var label = labelRepository.findAll().get(0);

        final var updateRequest
                = put(BASE_URL + LABEL_CONTROLLER_PATH + ID,
                label.getId())
                .content(TestUtils.asJson(TestUtils.LABEL_DTO_2))
                .contentType(APPLICATION_JSON);

        utils.perform(updateRequest, TestUtils.TEST_USERNAME);

        assertTrue(labelRepository.existsById(label.getId()));
        assertNotNull(labelRepository.findByName(TestUtils.LABEL_DTO_2.getName()).orElse(null));
        assertNull(labelRepository.findByName(TestUtils.LABEL_DTO.getName()).orElse(null));
    }

    @Test
    void deleteLabel() throws Exception {
        assertEquals(0, labelRepository.count());
        utils.regDefaultUser();
        utils.regDefaultLabel(TestUtils.TEST_USERNAME);

        final Long labelId = labelRepository.findByName("label1").get().getId();
        utils.perform(delete(BASE_URL + LABEL_CONTROLLER_PATH + ID, labelId),
                        TestUtils.TEST_USERNAME)
                .andExpect(status().isOk());
        assertEquals(0, labelRepository.count());
    }


    @Test
    public void getLabelByIdFails() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_USERNAME);
        final Label expectedLabel = labelRepository.findAll().get(0);
        Exception exception = assertThrows(
                Exception.class, () -> utils.perform(get(BASE_URL + LABEL_CONTROLLER_PATH + ID,
                        expectedLabel.getId()))
        );
        String message = exception.getMessage();
        assertTrue(message.contains("No value present"));
    }

    @Test
    public void regSameLabelTwice() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_USERNAME).andExpect(status().isCreated());
        utils.regDefaultLabel(TEST_USERNAME).andExpect(status().isUnprocessableEntity());

        assertEquals(1, labelRepository.count());
    }

    @Test
    public void deleteLabelFails() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultLabel(TEST_USERNAME);
        final Long labelId = labelRepository.findAll().get(0).getId() + 1;
        utils.perform(delete(BASE_URL + LABEL_CONTROLLER_PATH + ID, labelId), TEST_USERNAME)
                .andExpect(status().isInternalServerError());
        assertEquals(1, labelRepository.count());
    }
}
