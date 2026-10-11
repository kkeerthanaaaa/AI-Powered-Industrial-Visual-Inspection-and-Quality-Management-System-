package com.industrialinspection.util;

import com.industrialinspection.exception.ValidationException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final List<String> ALLOWED_IMAGE_EXTS = Arrays.asList("jpg", "jpeg", "png", "bmp");
    private static final long MAX_IMAGE_SIZE_BYTES = 25 * 1024 * 1024; // 25 MB

    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validatePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer greater than zero.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        validateNotEmpty(email, "Email address");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validateConfidence(double confidence) throws ValidationException {
        if (confidence < 0.0 || confidence > 100.0) {
            throw new ValidationException("AI Confidence Score must be between 0.0% and 100.0%.");
        }
    }

    public static void validateImageFile(File file) throws ValidationException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new ValidationException("Selected image file does not exist or is invalid.");
        }

        String name = file.getName().toLowerCase();
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx == -1) {
            throw new ValidationException("File has no extension. Allowed formats: JPG, JPEG, PNG, BMP.");
        }

        String ext = name.substring(dotIdx + 1);
        if (!ALLOWED_IMAGE_EXTS.contains(ext)) {
            throw new ValidationException("Unsupported file type '." + ext + "'. Allowed formats: JPG, JPEG, PNG, BMP.");
        }

        if (file.length() > MAX_IMAGE_SIZE_BYTES) {
            throw new ValidationException("Image file size (" + (file.length() / (1024 * 1024)) + 
                                          "MB) exceeds the maximum allowed limit of 25MB.");
        }
    }
}
