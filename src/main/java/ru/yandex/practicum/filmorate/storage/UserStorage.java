package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    void deleteUser(int id);

    List<User> getAllUsers();

    User getUserById(int id);

    List<User> getUsersByIds(Set<Long> ids);

    List<User> getCommonFriends(Set<Long> userFriends, Set<Long> otherUserFriends);
}
