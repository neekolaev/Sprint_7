package org.example;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OrderListTest {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String ORDER_LIST_ENDPOINT = "/api/v1/orders";

    @Test
    public void testGetOrderListSuccessfully() {
        Response response = fetchOrderList();
        validateOrderListResponse(response, 200);
    }

    @Step("Fetch the list of orders from the endpoint")
    private Response fetchOrderList() {
        return io.restassured.RestAssured
                .given()
                .baseUri(BASE_URL)
                .get(ORDER_LIST_ENDPOINT);
    }

    @Step("Validate the response: expected status code {expectedStatus}")
    private void validateOrderListResponse(Response response, int expectedStatus) {
        assertEquals("Unexpected status code", expectedStatus, response.statusCode());
        validateOrdersInResponse(response);
    }

    @Step("Ensure the response contains a non-empty list of orders")
    private void validateOrdersInResponse(Response response) {
        int orderCount = response.jsonPath().getList("orders").size();
        assertFalse("The order list should not be empty", orderCount == 0);
    }

}