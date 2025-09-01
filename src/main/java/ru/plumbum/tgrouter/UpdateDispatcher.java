package ru.plumbum.tgrouter;

import org.telegram.telegrambots.meta.api.objects.*;
import ru.plumbum.tgrouter.internal.HandlerEntry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class UpdateDispatcher {

    private final UpdateHandlerRegistry registry;

    public UpdateDispatcher(UpdateHandlerRegistry registry) {
        this.registry = registry;
    }

    public boolean dispatch(Update update) {
        List<HandlerEntry> candidates = new ArrayList<>();
        UpdateType type = detectType(update);
        candidates.addAll(registry.byType(type));
        candidates.addAll(registry.byType(UpdateType.ANY));

        for (HandlerEntry h : candidates) {
            if (matches(h, update)) {
                invoke(h, update);
                return true;
            }
        }
        return false;
    }

    private boolean matches(HandlerEntry h, Update u) {
        if (h.pattern == null || h.pattern.isBlank()) return true;
        String actual = extractString(h.type, u);
        if (actual == null) return false;
        return h.regex ? actual.matches(h.pattern) : actual.equals(h.pattern);
    }

    private String extractString(UpdateType type, Update u) {
        Message m = u.getMessage();
        Message em = u.getEditedMessage();

        return switch (type) {
            case MESSAGE_TEXT -> (u.hasMessage() && m.hasText()) ? m.getText() : null;
            case MESSAGE_CAPTION -> (u.hasMessage() ? m.getCaption() : null);
            case EDITED_MESSAGE_TEXT -> (u.hasEditedMessage() && em.hasText()) ? em.getText() : null;
            case EDITED_MESSAGE_CAPTION -> (u.hasEditedMessage() ? em.getCaption() : null);
            case CALLBACK_DATA -> u.hasCallbackQuery() ? u.getCallbackQuery().getData() : null;
            case INLINE_QUERY -> u.hasInlineQuery() ? u.getInlineQuery().getQuery() : null;
            case CHOSEN_INLINE_RESULT -> u.hasChosenInlineQuery() ? u.getChosenInlineQuery().getQuery() : null;
            case SHIPPING_QUERY -> u.hasShippingQuery() ? u.getShippingQuery().getInvoicePayload() : null;
            case PRE_CHECKOUT_QUERY -> u.hasPreCheckoutQuery() ? u.getPreCheckoutQuery().getInvoicePayload() : null;
            default -> (u.hasMessage() ? m.getCaption() : null); // для медиа — caption
        };
    }

    private UpdateType detectType(Update u) {
        Message m = u.getMessage();
        Message em = u.getEditedMessage();
        if (u.hasCallbackQuery()) return UpdateType.CALLBACK_DATA;
        if (u.hasInlineQuery()) return UpdateType.INLINE_QUERY;
        if (u.hasChosenInlineQuery()) return UpdateType.CHOSEN_INLINE_RESULT;
        if (u.hasShippingQuery()) return UpdateType.SHIPPING_QUERY;
        if (u.hasPreCheckoutQuery()) return UpdateType.PRE_CHECKOUT_QUERY;
        if (u.hasChatMember()) return UpdateType.CHAT_MEMBER;
        if (u.hasMyChatMember()) return UpdateType.MY_CHAT_MEMBER;
        if (u.hasChatJoinRequest()) return UpdateType.CHAT_JOIN_REQUEST;

        if (u.hasEditedMessage()) {
            if (em.hasText()) return UpdateType.EDITED_MESSAGE_TEXT;
            if (em.getCaption()!=null) return UpdateType.EDITED_MESSAGE_CAPTION;
        }

        if (u.hasMessage()) {
            if (m.hasText()) return UpdateType.MESSAGE_TEXT;
            if (m.hasPhoto()) return UpdateType.MESSAGE_PHOTO;
            if (m.hasVideo()) return UpdateType.MESSAGE_VIDEO;
            if (m.hasAudio()) return UpdateType.MESSAGE_AUDIO;
            if (m.hasVoice()) return UpdateType.MESSAGE_VOICE;
            if (m.hasDocument()) return UpdateType.MESSAGE_DOCUMENT;
            if (m.getAnimation()!=null) return UpdateType.MESSAGE_ANIMATION;
            if (m.getSticker()!=null) return UpdateType.MESSAGE_STICKER;
            if (m.getVideoNote()!=null) return UpdateType.MESSAGE_VIDEO_NOTE;
            if (m.getContact()!=null) return UpdateType.MESSAGE_CONTACT;
            if (m.getLocation()!=null) return UpdateType.MESSAGE_LOCATION;
            if (m.getVenue()!=null) return UpdateType.MESSAGE_VENUE;
            if (m.getPoll()!=null) return UpdateType.MESSAGE_POLL;
            if (m.getDice()!=null) return UpdateType.MESSAGE_DICE;
            if (m.getCaption()!=null) return UpdateType.MESSAGE_CAPTION;
        }

        return UpdateType.ANY;
    }

    private void invoke(HandlerEntry h, Update u) {
        try {
            h.method.invoke(h.bean, u);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Ошибка вызова " + h.method, e);
        }
    }
}
