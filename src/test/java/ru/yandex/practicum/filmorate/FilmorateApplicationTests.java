package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

	@Autowired
	private FilmController filmController;

	@Autowired
	private UserController userController;

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	private Film film;
	private User user;

	@BeforeEach
	void setUp() {
		film = new Film();
		user = new User();
	}

	@Test
	void filmValidationTest() {
		film.setName("");
		Set<ConstraintViolation<Film>> violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Название фильма должно быть заполнено.");

		film.setName("Название");
		film.setDescription("A".repeat(201));
		violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Описание фильма не может превышать 200 символов.");

		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(3000, 1, 1));
		violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Дата релиза не должна быть в будущем.");

		film.setReleaseDate(LocalDate.of(2020, 1, 1));
		film.setDuration(-10);
		violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Продолжительность фильма должна быть положительным числом.");

		film.setDuration(120);
		violations = validator.validate(film);
		assertTrue(violations.isEmpty(), "Фильм должен быть валидным.");

		film.setName("Valid Film");
		film.setDescription("Valid Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);
		violations = validator.validate(film);
		assertTrue(violations.isEmpty(), "Фильм должен быть валидным.");

		film.setDuration(0);
		violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Продолжительность фильма должна быть положительным числом.");
	}

	@Test
	void filmNameDefaultTest() {
		film.setName(null);
		film.setDescription("Описание фильма");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Film createdFilm = filmController.addFilm(film).getBody();

		assertNotNull(createdFilm);
		assertEquals("Название по умолчанию", createdFilm.getName(), "Имя фильма должно быть 'Название по умолчанию' по умолчанию, если не задано.");
	}

	@Test
	void userValidationTest() {
		user.setEmail("");
		user.setLogin("validLogin");
		user.setBirthday(LocalDate.of(2000, 1, 1));
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertFalse(violations.isEmpty(), "Электронная почта не может быть пустой.");

		user.setEmail("invalid");
		violations = validator.validate(user);
		assertFalse(violations.isEmpty(), "Электронная почта должна быть валидной.");

		user.setEmail("user@user.com");
		user.setLogin("");
		violations = validator.validate(user);
		assertFalse(violations.isEmpty(), "Логин должен быть заполнен.");

		user.setLogin("invalid login");
		violations = validator.validate(user);
		assertFalse(violations.isEmpty(), "Логин не должен содержать пробелы.");

		user.setLogin("validLogin");
		user.setBirthday(LocalDate.of(3000, 1, 1));
		violations = validator.validate(user);
		assertFalse(violations.isEmpty(), "Дата рождения не может быть в будущем.");

		user.setBirthday(LocalDate.of(2000, 1, 1));
		violations = validator.validate(user);
		assertTrue(violations.isEmpty(), "Пользователь должен быть валидным.");

		user.setName(null);
		assertEquals("validLogin", user.getName(), "Имя отображения должно быть логином, если не задано.");

		user.setName("Custom Name");
		assertEquals("Custom Name", user.getName(), "Имя отображения должно быть заданным именем.");
	}

	@Test
	void userUpdateValidationTest() {
		user.setId(100);
		Exception exception = assertThrows(ValidationException.class, () -> {
			userController.updateUser(user);
		});
		assertEquals("Пользователь с таким ID не найден.", exception.getMessage());
	}

	@Test
	void filmUpdateValidationTest() {
		film.setId(100);
		Exception exception = assertThrows(ValidationException.class, () -> {
			filmController.updateFilm(film);
		});
		assertEquals("Фильм с таким ID не найден.", exception.getMessage());
	}
}
