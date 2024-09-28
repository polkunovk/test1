package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final List<Film> films = new ArrayList<>();
    private int currentId = 1;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильма: {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            film.setName("Название по умолчанию");
        }

        LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза отсутствует.");
            throw new ValidationException("Дата релиза не может отсутствовать.");
        } else if (film.getReleaseDate().isBefore(earliestReleaseDate)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        } else if (film.getReleaseDate().isAfter(LocalDate.now())) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть в будущем.");
        }

        film.setId(currentId++);
        films.add(film);
        log.info("Фильм успешно добавлен: {}", film);
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма: {}", film);

        if (film.getId() <= 0 || film.getId() > currentId - 1) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
            throw new ValidationException("Фильм с таким ID не найден.");
        }

        films.set(film.getId() - 1, film);
        log.info("Фильм успешно обновлен: {}", film);
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов.");
        return films;
    }
}
