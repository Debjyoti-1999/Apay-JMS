package com.amazon.apayjiramanagementservice.lambda.calculator;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A basic calculator Lambda.
 */
public class Calculator {

    private ObjectMapper objectMapper;
    private static final Logger LOGGER = LogManager.getLogger(Calculator.class);

    static class Operands {
        private double x;
        private double y;

        Operands(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }
    }

    /**
     * Adds two operands, x and y, together.
     *
     * @param inputStream Input stream containing the operands
     * @param outputStream Where the results are written to
     * @param context Details about the invocation and the function
     * @throws IOException If there is an I/O error
     */
    public void add(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LOGGER.info("Beginning add()");

        // Get our inputs
        Operands operands = this.getOperandsFrom(inputStream);
        if (operands == null) {
            respond(null, outputStream);
            return;
        }
        LOGGER.info("Requested the addition of " + operands.x + " by " + operands.y);

        // Find the result
        double result = operands.x + operands.y;
        LOGGER.info("Result: " + result);

        respond(result, outputStream);
    }

    /**
     * Parses the operands from the input stream.
     *
     * @param inputStream Byte stream containing the operands
     * @return An Operand object
     * @throws IOException If an error occurs while reading the inputStream
     */
    public Operands getOperandsFrom(InputStream inputStream) throws IOException {
        /*
        Example request:
        {
            "x": "1",
            "y": "2"
        }
        */
        if (this.objectMapper == null) {
            this.objectMapper = new ObjectMapper();
        }

        JsonNode request = this.objectMapper.readTree(inputStream);
        if (request == null || !request.has("x") || !request.has("y")) {
            return null;
        }

        return new Operands(request.get("x").asDouble(), request.get("y").asDouble());
    }

    /**
     * Writes the results to an output stream.
     *
     * @param result Result of an operation
     * @param outputStream Where the result is written to
     * @throws IOException If an error occurs while writing the response
     */
    public void respond(Double result, OutputStream outputStream) throws IOException {
        String statusCode = "200";
        if (result == null) {
            statusCode = "400";
        }

        if (this.objectMapper == null) {
            this.objectMapper = new ObjectMapper();
        }

        ObjectNode response = this.objectMapper.createObjectNode();
        response.put("statusCode", statusCode);
        response.put("body", String.valueOf(result));
        String responseString = ((JsonNode) response).toString();

        LOGGER.info("Response: " + responseString);

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseString);
        writer.close();
    }
}
