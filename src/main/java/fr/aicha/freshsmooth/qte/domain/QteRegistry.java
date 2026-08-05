package fr.aicha.freshsmooth.qte.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class QteRegistry {
    private final Map<String, QteDefinition> definitions = new LinkedHashMap<>();

    public boolean add(QteDefinition definition) {
        return definitions.putIfAbsent(definition.id(), definition) == null;
    }

    public void put(QteDefinition definition) {
        definitions.put(definition.id(), definition);
    }

    public Optional<QteDefinition> find(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public boolean remove(String id) {
        return definitions.remove(id) != null;
    }

    public List<String> ids() {
        return definitions.keySet().stream().sorted().toList();
    }

    public Collection<QteDefinition> values() {
        return List.copyOf(definitions.values());
    }
}
