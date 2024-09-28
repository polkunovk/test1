package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
//import java.util.List;

@Service
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()) == null) {
            log.warn("Фильм с ID {} для обновления не найден.", film.getId());
            throw new ValidationException("Фильм с таким ID не найден.");
        }

        validateFilm(film);
        return filmStorage.updateFilm(film);
    }


    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            log.warn("Фильм с ID {} не найден.", id);
            throw new ValidationException("Фильм с таким ID не найден.");
        }
        return film;
    }


    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Нет названия фильма или оно пустое. Название по умолчанию.");
            film.setName("Название по умолчанию");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза отсутствует.");
            throw new ValidationException("Дата релиза не может отсутствовать.");
        } else if (film.getReleaseDate().isBefore(earliestReleaseDate)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }

        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    public void addLike(int filmId, Long userId) {
        Film film = getFilmById(filmId);
        film.addLike(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new ValidationException("Фильм с таким ID не найден.");
        }

        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new ValidationException("Пользователь не найден.");
        }

        if (!film.getLikes().contains((long) userId)) {
            throw new ValidationException("Лайк не найден");
        }

        film.getLikes().remove((long) userId);
    }

}
