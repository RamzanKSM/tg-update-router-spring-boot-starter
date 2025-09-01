package ru.plumbum.tgrouter.internal;

import ru.plumbum.tgrouter.UpdateType;
import java.lang.reflect.Method;

public final class HandlerEntry {
    public final Object bean;
    public final Method method;
    public final UpdateType type;
    public final String pattern;
    public final boolean regex;
    public final int order;

    public HandlerEntry(Object bean, Method method, UpdateType type,
                        String pattern, boolean regex, int order) {
        this.bean = bean;
        this.method = method;
        this.type = type;
        this.pattern = pattern;
        this.regex = regex;
        this.order = order;
    }
}
