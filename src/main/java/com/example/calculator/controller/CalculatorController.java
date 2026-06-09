package com.example.calculator.controller;

import com.example.calculator.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    @Autowired
    private CalculatorService calculatorService;

    @GetMapping("/add")
    public ResponseEntity<Map<String, Object>> add(
            @RequestParam double a,
            @RequestParam double b) {
        double result = calculatorService.add(a, b);
        return ResponseEntity.ok(createResponse(a, b, result, "addition"));
    }

    @GetMapping("/subtract")
    public ResponseEntity<Map<String, Object>> subtract(
            @RequestParam double a,
            @RequestParam double b) {
        double result = calculatorService.subtract(a, b);
        return ResponseEntity.ok(createResponse(a, b, result, "subtraction"));
    }

    @GetMapping("/multiply")
    public ResponseEntity<Map<String, Object>> multiply(
            @RequestParam double a,
            @RequestParam double b) {
        double result = calculatorService.multiply(a, b);
        return ResponseEntity.ok(createResponse(a, b, result, "multiplication"));
    }

    @GetMapping("/divide")
    public ResponseEntity<Map<String, Object>> divide(
            @RequestParam double a,
            @RequestParam double b) {
        try {
            double result = calculatorService.divide(a, b);
            return ResponseEntity.ok(createResponse(a, b, result, "division"));
        } catch (ArithmeticException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/power")
    public ResponseEntity<Map<String, Object>> power(
            @RequestParam double base,
            @RequestParam double exponent) {
        double result = calculatorService.power(base, exponent);
        Map<String, Object> response = new HashMap<>();
        response.put("base", base);
        response.put("exponent", exponent);
        response.put("result", result);
        response.put("operation", "power");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sqrt")
    public ResponseEntity<Map<String, Object>> squareRoot(
            @RequestParam double number) {
        try {
            double result = calculatorService.squareRoot(number);
            Map<String, Object> response = new HashMap<>();
            response.put("number", number);
            response.put("result", result);
            response.put("operation", "square root");
            return ResponseEntity.ok(response);
        } catch (ArithmeticException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Calculator API");
        response.put("version","1.0.0");
        response.put("developer","roshankhatri");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createResponse(double a, double b, double result, String operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("operand1", a);
        response.put("operand2", b);
        response.put("result", result);
        response.put("operation", operation);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", "failed");
        return response;
    }
}
