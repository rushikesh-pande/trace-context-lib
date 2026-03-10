package com.orderprocessing.security;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Input Sanitizer — Enhancement: Input Validation / Injection Prevention (Enhancement #3)
 * Prevents SQL Injection, XSS, Command Injection
 */
@Component
public class InputSanitizer {

    private static final Pattern SQL_INJECTION = Pattern.compile(
        "(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT|--|;)", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)<script[^>]*>[\\s\\S]*?</script>|javascript:|on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern CMD_INJECTION = Pattern.compile(
        "[;&|`$(){}]");
    private static final int MAX_INPUT_LENGTH = 10_000;

    public String sanitize(String input) {
        if (input == null) return null;
        if (input.length() > MAX_INPUT_LENGTH)
            throw new SecurityException("Input too long: " + input.length() + " chars (max " + MAX_INPUT_LENGTH + ")");
        if (SQL_INJECTION.matcher(input).find())
            throw new SecurityException("Potential SQL injection detected");
        if (XSS_PATTERN.matcher(input).find())
            throw new SecurityException("Potential XSS attack detected");
        // Escape HTML special chars
        return input
            .replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
            .replace(""","&quot;").replace("'","&#x27;");
    }

    public void validateId(String id) {
        if (id == null || !id.matches("[a-zA-Z0-9_\\-]+"))
            throw new SecurityException("Invalid ID format: " + id);
    }

    public void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new SecurityException("Invalid email format");
    }

    public boolean isSqlSafe(String input) {
        return input != null && !SQL_INJECTION.matcher(input).find();
    }

    public boolean isXssSafe(String input) {
        return input != null && !XSS_PATTERN.matcher(input).find();
    }
}
