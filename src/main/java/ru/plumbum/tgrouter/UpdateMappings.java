package ru.plumbum.tgrouter;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UpdateMappings {
    UpdateMapping[] value();
}
