package com.amazon.apayjiramanagementservice.lambda.calculator;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorTest {

    private final Calculator.Operands goodOperands = new Calculator.Operands(8.0, 2.0);
    private final Calculator.Operands badOperands = null;

    private Calculator calculator;
    private Calculator spyCalculator;
    private InputStream inputStream;
    private InputStream badRequestInputStream;
    private OutputStream outputStream;
    private Context mockContext;

    @BeforeEach
    public void setup() throws IOException {
        // Set up our test variables, mocks, etc...
        calculator = new Calculator();
        spyCalculator = Mockito.spy(calculator);

        inputStream = inputStreamFrom("fake good request");
        Mockito.doReturn(goodOperands).when(spyCalculator).getOperandsFrom(inputStream);

        badRequestInputStream = inputStreamFrom("fake bad request");
        Mockito.doReturn(badOperands).when(spyCalculator).getOperandsFrom(badRequestInputStream);

        outputStream = new ByteArrayOutputStream();
        mockContext = Mockito.mock(Context.class);
    }

    /*
     * Happy path
     */
    @Test
    public void WHEN_add_called_THEN_responds_correct_value() throws IOException {
        spyCalculator.add(inputStream, outputStream, mockContext);
        Mockito.verify(spyCalculator).respond(10.0, outputStream);
    }

    /*
     * Bad requests
     */
    @Test
    public void WHEN_add_called_bad_data_THEN_responds_null() throws IOException {
        spyCalculator.add(badRequestInputStream, outputStream, mockContext);
        Mockito.verify(spyCalculator).respond(null, outputStream);
    }

    /*
     * Test helpers
     */

    @Test
    public void WHEN_getOperands_called_THEN_parses() throws IOException {
        String request = "{\"x\":\"8\", \"y\":\"2\"}";
        Calculator.Operands operands = calculator.getOperandsFrom(inputStreamFrom(request));
        assertEquals(8.0, operands.getX(), 0.1);
        assertEquals(2.0, operands.getY(), 0.1);
    }

    @Test
    public void WHEN_getOperands_missing_x_THEN_returns_null() throws IOException {
        String request = "{\"y\":\"8\"}";
        Calculator.Operands operands = calculator.getOperandsFrom(inputStreamFrom(request));
        assertEquals(operands, null);
    }

    @Test
    public void WHEN_getOperands_missing_y_THEN_returns_null() throws IOException {
        String request = "{\"x\":\"8\"}";
        Calculator.Operands operands = calculator.getOperandsFrom(inputStreamFrom(request));
        assertEquals(operands, null);
    }

    @Test
    public void WHEN_getOperands_no_request_THEN_returns_null() throws IOException {
        String request = "{}";
        Calculator.Operands operands = calculator.getOperandsFrom(inputStreamFrom(request));
        assertEquals(operands, null);
    }

    @Test
    public void WHEN_respond_with_result_THEN_returns_200() throws IOException {
        calculator.respond(2.0, outputStream);

        String expectedOutput = "{\"statusCode\":\"200\",\"body\":\"2.0\"}";
        assertEquals(expectedOutput, stringFrom(outputStream));
    }

    @Test
    public void WHEN_respond_with_no_result_THEN_returns_400() throws IOException {
        calculator.respond(null, outputStream);

        String expectedOutput = "{\"statusCode\":\"400\",\"body\":\"null\"}";
        assertEquals(expectedOutput, stringFrom(outputStream));
    }

    private String stringFrom(OutputStream outputStream) {
        return new String(((ByteArrayOutputStream)outputStream).toByteArray(), StandardCharsets.UTF_8);
    }

    private InputStream inputStreamFrom(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }
}
