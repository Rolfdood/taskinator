package com.taskinator.taskinator.common.validation;

import java.util.regex.Pattern;

public final class ValidationPatterns {

    private ValidationPatterns() {}

    public static final Pattern EMAIL = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public static final Pattern PASSWORD = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );
}