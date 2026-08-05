package fr.aicha.freshsmooth.qte.server;

import fr.aicha.freshsmooth.qte.domain.QteDefinition;
import fr.aicha.freshsmooth.qte.domain.QteRegistry;
import fr.aicha.freshsmooth.qte.domain.QteType;
import fr.aicha.freshsmooth.qte.domain.QteKeyMigration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class QteSavedData extends SavedData {
    private static final int FORMAT_VERSION = 3;
    private static final String FILE_NAME = "qte_engine_definitions";
    private static final Factory<QteSavedData> FACTORY = new Factory<>(QteSavedData::new, QteSavedData::load);

    private final QteRegistry registry = new QteRegistry();

    public static QteSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, FILE_NAME);
    }

    public QteRegistry registry() {
        return registry;
    }

    public boolean add(QteDefinition definition) {
        boolean changed = registry.add(definition);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean remove(String id) {
        boolean changed = registry.remove(id);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("formatVersion", FORMAT_VERSION);
        ListTag definitions = new ListTag();
        for (QteDefinition definition : registry.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", definition.id());
            entry.putString("type", definition.type().name());
            entry.putString("keys", String.join(",", definition.keys()));
            entry.putInt("durationTicks", definition.durationTicks());
            entry.putString("resultCommand", definition.resultCommand());
            entry.putBoolean("exclusiveInput", definition.exclusiveInput());
            if (definition.texture() != null) {
                entry.putString("texture", definition.texture());
            }
            definitions.add(entry);
        }
        tag.put("definitions", definitions);
        return tag;
    }

    private static QteSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        QteSavedData data = new QteSavedData();
        boolean legacyFormat = tag.getInt("formatVersion") < FORMAT_VERSION;
        ListTag definitions = tag.getList("definitions", Tag.TAG_COMPOUND);
        for (int index = 0; index < definitions.size(); index++) {
            CompoundTag entry = definitions.getCompound(index);
            try {
                String texture = entry.contains("texture", Tag.TAG_STRING) ? entry.getString("texture") : null;
                String serializedKeys = entry.contains("keys", Tag.TAG_STRING)
                    ? entry.getString("keys")
                    : entry.getString("pattern");
                data.registry.put(new QteDefinition(
                    entry.getString("id"),
                    QteType.parse(entry.getString("type")),
                    java.util.Arrays.stream(serializedKeys.split(","))
                        .map(key -> QteKeyMigration.migrate(key, legacyFormat))
                        .toList(),
                    entry.getInt("durationTicks"),
                    entry.getString("resultCommand"),
                    entry.getBoolean("exclusiveInput"),
                    texture
                ));
            } catch (IllegalArgumentException ignored) {
                // A malformed entry is skipped so one broken definition cannot make a world unloadable.
            }
        }
        return data;
    }
}
