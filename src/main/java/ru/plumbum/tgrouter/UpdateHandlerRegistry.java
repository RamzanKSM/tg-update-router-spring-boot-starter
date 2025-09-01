package ru.plumbum.tgrouter;

import ru.plumbum.tgrouter.internal.HandlerEntry;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateHandlerRegistry {
    private final List<HandlerEntry> entries = new CopyOnWriteArrayList<>();
    private volatile Map<UpdateType, List<HandlerEntry>> byType = Map.of();

    public void register(HandlerEntry e) {
        entries.add(e);
        rebuildIndex();
    }

    public List<HandlerEntry> byType(UpdateType t) {
        return byType.getOrDefault(t, List.of());
    }

    private synchronized void rebuildIndex() {
        entries.sort(Comparator.comparingInt(h -> h.order));
        Map<UpdateType, List<HandlerEntry>> map = new EnumMap<>(UpdateType.class);
        for (var e : entries) {
            map.computeIfAbsent(e.type, k -> new ArrayList<>()).add(e);
        }
        this.byType = map;
    }
}
