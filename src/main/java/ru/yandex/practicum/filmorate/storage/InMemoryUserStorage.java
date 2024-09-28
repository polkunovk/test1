package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 1;

    public void clear() {
        users.clear();
        currentId = 1;
    }

    @Override
    public User addUser(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        } else {
            throw new IllegalArgumentException("Пользователь с ID " + user.getId() + " не найден.");
        }
    }

    @Override
    public void deleteUser(int id) {
        if (!users.containsKey(id)) {
            throw new IllegalArgumentException("Пользователь с ID " + id + " не найден.");
        }
        users.remove(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getUsersByIds(Set<Long> ids) {
        return users.values().stream()
                .filter(user -> ids.contains((long) user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Set<Long> userFriends, Set<Long> otherUserFriends) {
        Set<Long> commonFriendsIds = new HashSet<>(userFriends);
        commonFriendsIds.retainAll(otherUserFriends);
        return getUsersByIds(commonFriendsIds);
    }
}
