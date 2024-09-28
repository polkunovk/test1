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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
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
	private User validUser;
	private InMemoryFilmStorage filmStorage;
	private InMemoryUserStorage userStorage;

	@BeforeEach
	public void setUp() {
		filmStorage = new InMemoryFilmStorage();
		userStorage = new InMemoryUserStorage();
		filmStorage.clear();
		userStorage.clear();

		film = new Film();
		user = new User();

		validUser = new User();
		validUser.setEmail("example@mail.com");
		validUser.setLogin("validLogin");
		validUser.setBirthday(LocalDate.of(2000, 1, 1));
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

		user.setName(null); // Имя не задано
		assertEquals("validLogin", user.getName(), "Имя отображения должно быть логином, если не задано.");

		user.setName("Name");
		assertEquals("Name", user.getName(), "Имя отображения должно быть заданным именем.");
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

	@Test
	void addFriendTest() {
		user.setId(1);
		user.setEmail("user1@example.com");
		user.setLogin("user1");
		user.setName("user 1");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user);

		User friend = new User();
		friend.setId(2);
		friend.setEmail("friend@friend.com");
		friend.setLogin("friend");
		friend.setName("friend");
		friend.setBirthday(LocalDate.of(1992, 2, 2));
		userController.addUser(friend);

		ResponseEntity<Void> response = userController.addFriend(user.getId(), friend.getId());
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Друг должен быть добавлен.");
	}


	@Test
	void addFriendUserNotFoundTest() {
		user.setId(1);
		user.setEmail("user@example.com");
		user.setLogin("user");
		user.setName("Юзер");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user);

		Exception exception = assertThrows(ValidationException.class, () -> {
			userController.addFriend(user.getId(), 999);
		});
		assertEquals("Пользователь с таким ID не найден.", exception.getMessage());
	}

	@Test
	void removeFriendTest() {
		user.setId(1);
		user.setEmail("user1@example.com");
		user.setLogin("user1");
		user.setName("Пользователь");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user);

		User friend = new User();
		friend.setId(2);
		friend.setEmail("friend@friend.com");
		friend.setLogin("friend");
		friend.setName("Друг");
		friend.setBirthday(LocalDate.of(1992, 2, 2));
		userController.addUser(friend);

		userController.addFriend(user.getId(), friend.getId());

		ResponseEntity<Void> response = userController.removeFriend(user.getId(), friend.getId());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Друг должен быть удален.");
	}

	@Test
	void removeFriendUserNotFoundTest() {
		user.setId(1);
		user.setEmail("user1@example.com");
		user.setLogin("user1");
		user.setName("User One");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user);

		Exception exception = assertThrows(ValidationException.class, () -> {
			userController.removeFriend(user.getId(), 999);
		});

		assertEquals("Пользователь с таким ID не найден.", exception.getMessage());
	}

	@Test
	void addLikeTest() {
		film.setId(1);
		film.setName("Фильм");
		film.setDescription("Фильм для примера.");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);
		filmController.addFilm(film);

		user.setId(1);
		user.setEmail("user1@example.com");
		user.setLogin("user1");
		user.setName("Юзер 1");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user);

		ResponseEntity<Void> response = filmController.addLike(film.getId(), (long) user.getId());
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Лайк должен быть добавлен.");
	}

	@Test
	void addLikeFilmNotFoundTest() {
		user.setId(1);
		user.setEmail("user1@example.com");
		user.setLogin("user1");
		user.setName("Юзер");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user);

		Exception exception = assertThrows(ValidationException.class, () -> {
			filmController.addLike(999, (long) user.getId());
		});
		assertEquals("Фильм с таким ID не найден.", exception.getMessage());
	}

	@Test
	void removeLikeTest() {
		Film film = new Film();
		film.setId(1);
		film.setName("Название фильма");
		film.setDescription("Описание фильма");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(120);

		filmController.addFilm(film);

		User user = new User();
		user.setId(1);
		user.setEmail("user@example.com");
		user.setLogin("userlogin");
		user.setName("Имя пользователя");
		user.setBirthday(LocalDate.now().minusYears(20));

		userController.addUser(user);

		filmController.addLike(film.getId(), (long) user.getId());

		ResponseEntity<Void> response = filmController.removeLike(film.getId(), user.getId());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Лайк должен быть удален.");
	}


}
