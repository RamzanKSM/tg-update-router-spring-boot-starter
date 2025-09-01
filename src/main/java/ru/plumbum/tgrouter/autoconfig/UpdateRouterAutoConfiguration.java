package ru.plumbum.tgrouter.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.plumbum.tgrouter.*;

@AutoConfiguration
@ConditionalOnClass(Update.class)
public class UpdateRouterAutoConfiguration {

    @Bean @ConditionalOnMissingBean
    public UpdateHandlerRegistry updateHandlerRegistry() {
        return new UpdateHandlerRegistry();
    }

    @Bean @ConditionalOnMissingBean
    public UpdateMappingBeanPostProcessor updateMappingBeanPostProcessor(UpdateHandlerRegistry registry) {
        return new UpdateMappingBeanPostProcessor(registry);
    }

    @Bean @ConditionalOnMissingBean
    public UpdateDispatcher updateDispatcher(UpdateHandlerRegistry registry) {
        return new UpdateDispatcher(registry);
    }
}
