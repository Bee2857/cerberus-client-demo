package com.ctrip.framework.cerberus.client.utils;

import java.util.Collection;

public class Args {
    public static <T> T notNull(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        return argument;
    }

    public static <T extends CharSequence> T notEmpty(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        if (isEmpty(argument)) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return argument;
    }

    public static <T extends CharSequence> T notBlank(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        if (isBlank(argument)) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return argument;
    }

    public static <T extends Collection> T notEmpty(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        if (argument.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return argument;
    }

    public static boolean isEmpty(final CharSequence s) {
        if (s == null) {
            return true;
        }
        return s.length() == 0;
    }

    public static boolean isBlank(final CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
