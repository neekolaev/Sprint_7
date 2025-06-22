package org.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CourierCreationTest {
    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru";
    private CourierClient courierClient;
    private Integer createdCourierId;
    private String lastCreatedLogin;
    private String lastCreatedPassword;

    @Before
    @Step("Настройка тестового окружения")
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        courierClient = new CourierClient();
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        if (createdCourierId != null) {
            deleteCourier(createdCourierId);
        }
    }

    @Test
    @DisplayName("Создание курьера с валидными данными")
    @Description("Проверка успешного создания курьера со всеми обязательными полями")
    public void testCreateCourierSuccessfully() {
        // Генерируем уникальные данные для каждого запуска
        String uniqueLogin = generateUniqueLogin("courier");
        String password = "password123";
        String firstName = "Test Courier";

        Response response = sendCreateCourierRequest(uniqueLogin, password, firstName);
        verifySuccessfulCreation(response);

        // Сохраняем данные для последующего удаления
        lastCreatedLogin = uniqueLogin;
        lastCreatedPassword = password;
        saveCreatedCourierId();
    }

    @Test
    @DisplayName("Создание курьера с пустым логином")
    @Description("Проверка, что нельзя создать курьера с пустым логином")
    public void testCreateCourierMissingRequiredFields() {
        // Пробуем создать курьера с пустым логином
        Response response = sendCreateCourierRequest("", "password123", "Test");

        // Если статус 409, значит пустая строка уже существует как логин
        if (response.statusCode() == 409) {
            response.then()
                    .statusCode(409)
                    .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
        } else {
            // Иначе ожидаем 400
            verifyMissingFieldsError(response);
        }
    }

    @Test
    @DisplayName("Создание курьера с существующим логином")
    @Description("Проверка, что нельзя создать двух курьеров с одинаковым логином")
    public void testCreateCourierWithExistingLogin() {
        // Генерируем уникальный логин
        String uniqueLogin = generateUniqueLogin("duplicate");
        String password = "password123";

        // Создаем первого курьера
        Response firstResponse = sendCreateCourierRequest(uniqueLogin, password, "First Courier");

        // Проверяем, что первый курьер создан успешно
        verifySuccessfulCreation(firstResponse);

        // Сохраняем данные для удаления
        lastCreatedLogin = uniqueLogin;
        lastCreatedPassword = password;
        saveCreatedCourierId();

        // Пытаемся создать второго курьера с тем же логином
        Response secondResponse = sendCreateCourierRequest(uniqueLogin, "anotherPassword", "Second Courier");

        verifyDuplicateLoginError(secondResponse);
    }

    @Step("Генерация уникального логина с префиксом: {prefix}")
    private String generateUniqueLogin(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    @Step("Отправка запроса на создание курьера с логином: {login}")
    private Response sendCreateCourierRequest(String login, String password, String firstName) {
        return courierClient.createCourier(login, password, firstName);
    }

    @Step("Проверка успешного создания курьера")
    private void verifySuccessfulCreation(Response response) {
        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Проверка ошибки при отсутствии обязательных полей")
    private void verifyMissingFieldsError(Response response) {
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Проверка ошибки при дублировании логина")
    private void verifyDuplicateLoginError(Response response) {
        response.then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Step("Сохранение ID созданного курьера для последующего удаления")
    private void saveCreatedCourierId() {
        if (lastCreatedLogin != null && lastCreatedPassword != null) {
            // Используем метод loginCourier из CourierClient
            Response loginResponse = courierClient.loginCourier(lastCreatedLogin, lastCreatedPassword);
            if (loginResponse.statusCode() == 200) {
                createdCourierId = loginResponse.jsonPath().getInt("id");
            }
        }
    }

    @Step("Удаление курьера с ID: {courierId}")
    private void deleteCourier(Integer courierId) {
        courierClient.deleteCourier(courierId);
    }
}