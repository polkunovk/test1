package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        validateUser(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()) == null) {
            log.warn("Пользователь с ID {} не найден для обновления.", user.getId());
            throw new ValidationException("Пользователь с таким ID не найден.");
        }
        validateUser(user);
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            log.warn("Пользователь с ID {} не найден.", id);
            throw new ValidationException("Пользователь с таким ID не найден.");
        }
        return user;
    }


    public void addFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.addFriend((long) friendId);
        friend.addFriend((long) userId);
        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.removeFriend((long) friendId);
        friend.removeFriend((long) userId);
        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Пустая электронная почта.");
            throw new ValidationException("Электронная почта не может быть пустой.");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Неправильно введена электронная почта: {}", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать '@'.");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы.");
        }

        if (user.getBirthday() == null) {
            log.warn("Дата рождения отсутствует.");
            throw new ValidationException("Дата рождения не может отсутствовать.");
        }

        if (user.getBirthday().isAfter(java.time.LocalDate.now())) {
            log.warn("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя отсутствует, вместо него установлен логин.");
            user.setName(user.getLogin());
        }
    }
}
