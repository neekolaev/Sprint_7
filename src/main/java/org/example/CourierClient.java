package org.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CourierClient {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private static final String COURIER_ENDPOINT = "/api/v1/courier";

    static {
        RestAssured.baseURI = BASE_URL;
    }

    /**
     * Method to create a courier.
     *
     * @param login    Courier login string.
     * @param password Courier password string.
     * @param firstName Courier first name.
     * @return Response object containing API response.
     */
    public Response createCourier(String login, String password, String firstName) {
        String requestBody = String.format(
                "{\"login\":\"%s\", \"password\":\"%s\", \"firstName\":\"%s\"}",
                login, password, firstName
        );

        return RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(COURIER_ENDPOINT);
    }

    /**
     * Method to login a courier.
     *
     * @param login    Courier login string.
     * @param password Courier password string.
     * @return Response object containing API response.
     */
    public Response loginCourier(String login, String password) {
        String requestBody;

        if (login == null && password == null) {
            requestBody = "{}";
        } else if (login == null) {
            requestBody = String.format("{\"password\":\"%s\"}", password);
        } else if (password == null) {
            requestBody = String.format("{\"login\":\"%s\"}", login);
        } else {
            requestBody = String.format(
                    "{\"login\":\"%s\", \"password\":\"%s\"}",
                    login, password
            );
        }

        return RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post(COURIER_ENDPOINT + "/login");
    }

    /**
     * Method to delete a courier by its ID.
     *
     * @param courierId The ID of the courier to delete.
     * @return Response object containing API response.
     */
    public Response deleteCourier(int courierId) {
        return RestAssured
                .given()
                .header("Content-Type", "application/json")
                .delete(COURIER_ENDPOINT + "/" + courierId);
    }
}