package org.example;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CourierLoginTest {

    private CourierClient courierClient;
    private int courierId;
    private String uniqueLogin;

    @Before
    public void setUp() {
        courierClient = new CourierClient();
        courierId = -1;
        // Генерируем уникальный логин для каждого теста
        uniqueLogin = "test_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @After
    public void tearDown() {
        if (courierId != -1) {
            deleteCourier(courierId);
        }
    }

    @Test
    public void testCourierLoginSuccessfully() {
        String password = "1234";

        // Создаем курьера
        Response createResponse = courierClient.createCourier(uniqueLogin, password, "TestName");
        assertEquals("Failed to create courier", 201, createResponse.statusCode());

        // Логинимся и проверяем
        loginCourierAndVerify(uniqueLogin, password, 200, null);
    }

    @Test
    public void testLoginWithIncorrectCredentials() {
        loginCourierAndVerify("invalidCourier", "wrongPassword", 404, "Учетная запись не найдена");
    }

    @Test
    public void testLoginWithMissingFields() {
        loginCourierAndVerify(null, "1234", 400, "Недостаточно данных для входа");
    }

    @Test
    public void testLoginNonExistentCourier() {
        loginCourierAndVerify("nonExistentUser", "1234", 404, "Учетная запись не найдена");
    }

    @Step("Удалить курьера с ID: {courierId}")
    private void deleteCourier(int courierId) {
        Response response = courierClient.deleteCourier(courierId);
        assertEquals("Failed to delete courier", 200, response.statusCode());
    }

    @Step("Авторизация курьера с логином: {login}, паролем: {password}")
    private void loginCourierAndVerify(String login, String password, int expectedStatus, String expectedMessage) {
        Response response = courierClient.loginCourier(login, password);
        verifyResponseStatus(response, expectedStatus, expectedMessage);

        if (expectedStatus == 200) {
            // Извлекаем ID только если логин успешен
            Integer id = response.jsonPath().getInt("id");
            if (id != null) {
                courierId = id;
            }
        }
    }

    @Step("Проверить ответ. Ожидаемый статус: {expectedStatus}, ожидаемое сообщение: {expectedMessage}")
    private void verifyResponseStatus(Response response, int expectedStatus, String expectedMessage) {
        assertEquals("Unexpected status code", expectedStatus, response.statusCode());
        if (expectedMessage != null) {
            String actualMessage = response.jsonPath().getString("message");
            assertEquals("Unexpected error message", expectedMessage, actualMessage);
        }
    }
}