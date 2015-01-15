package ru.ppsrk.gwt.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresRoles {
    public static final String ADMIN = "admin";
    public static final String DEPT = "dept";
    public static final String USER = "user";

    public String[] value();
}