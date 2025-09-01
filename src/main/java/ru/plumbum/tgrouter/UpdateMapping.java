package ru.plumbum.tgrouter;

import java.lang.annotation.*;

@Repeatable(UpdateMappings.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UpdateMapping {
    UpdateType type();
    String value() default "";
    boolean regex() default false;
    int order() default 0;
}
