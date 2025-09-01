package ru.plumbum.tgrouter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;
import ru.plumbum.tgrouter.internal.HandlerEntry;

public class UpdateMappingBeanPostProcessor implements BeanPostProcessor {

    private final UpdateHandlerRegistry registry;

    public UpdateMappingBeanPostProcessor(UpdateHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), m -> {
            var container = AnnotatedElementUtils.findMergedAnnotation(m, UpdateMappings.class);
            var single    = AnnotatedElementUtils.findMergedAnnotation(m, UpdateMapping.class);

            UpdateMapping[] anns = (container != null) ? container.value()
                    : (single != null ? new UpdateMapping[]{single} : new UpdateMapping[0]);

            if (anns.length == 0) return;

            // Ровно один параметр: org.telegram ... Update
            if (m.getParameterCount() != 1 ||
                !org.telegram.telegrambots.meta.api.objects.Update.class.isAssignableFrom(m.getParameterTypes()[0])) {
                throw new IllegalStateException(bean.getClass().getName() + "#" + m.getName()
                        + " должен принимать один параметр org.telegram.telegrambots.meta.api.objects.Update");
            }

            m.setAccessible(true);
            for (var ann : anns) {
                registry.register(new HandlerEntry(bean, m, ann.type(), ann.value(), ann.regex(), ann.order()));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
        return bean;
    }
}
