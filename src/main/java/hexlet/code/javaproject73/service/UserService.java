package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.UserDto;
import hexlet.code.javaproject73.model.User;

public interface UserService {

    User createNewUser(UserDto userDto);

    User updateUser(long id, UserDto userDto);

    User getCurrentUser();
}
