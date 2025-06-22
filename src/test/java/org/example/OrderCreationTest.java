package org.example;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrderCreationTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String ORDER_ENDPOINT = "/api/v1/orders";

    @Test
    public void testCreateOrderWithBlackColor() {
        String[] color = {"BLACK"};
        Response response = createOrder(color);
        validateOrderResponse(response, 201);
    }

    @Test
    public void testCreateOrderWithGreyColor() {
        String[] color = {"GREY"};
        Response response = createOrder(color);
        validateOrderResponse(response, 201);
    }

    @Test
    public void testCreateOrderWithBothColors() {
        String[] color = {"BLACK", "GREY"};
        Response response = createOrder(color);
        validateOrderResponse(response, 201);
    }

    @Test
    public void testCreateOrderWithoutColor() {
        Response response = createOrder(null);
        validateOrderResponse(response, 201);
    }

    @Step("Create an order with colors: {color}")
    private Response createOrder(String[] color) {
        String requestBody = (color == null) ? "{}" : String.format("{\"color\": [\"%s\"]}", String.join("\", \"", color));
        return io.restassured.RestAssured
                .given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(ORDER_ENDPOINT);
    }

    @Step("Validate the order response with status code: {expectedStatusCode}")
    private void validateOrderResponse(Response response, int expectedStatusCode) {
        assertEquals("Unexpected status code", expectedStatusCode, response.statusCode());
        validateResponseTrack(response);
    }

    @Step("Validate the response contains a non-null 'track' field")
    private void validateResponseTrack(Response response) {
        Integer track = response.jsonPath().getInt("track");
        assertNotNull("The 'track' field should not be null", track);
    }

}