package com.betterbrainmemory.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.betterbrainmemory.opentoday.app.FeatureFlag;
import com.betterbrainmemory.opentoday.app.Registry;
import com.betterbrainmemory.opentoday.app.data.CherryOrchard;
import com.betterbrainmemory.opentoday.util.Identifier;
import com.betterbrainmemory.opentoday.util.RandomUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Registry of items. Contains links between the following:
 * <p>* Java class. E.g. TextItem.class</p>
 * <p>* String type. E.g. "TextItem"</p>
 * <p>* ItemType enum</p>
 * <p>* Codec. See {@link AbstractItemCodec} E.g. TextItem.CODEC</p>
 * <p>* Instructions for creating an empty instance</p>
 * <p>* Instructions for copying</p>
 */
public class ItemsRegistry extends Registry<Identifier, ItemsRegistry.ItemInfo> {
    @NonNull
    public static final ItemsRegistry REGISTRY = new ItemsRegistry();

    private static final ItemInfo[] NO_PARENTS = new ItemInfo[0];
    @NonNull
    private final LinkedHashMap<Identifier, ItemInfo> ITEMS = new LinkedHashMap<>();
    private final LinkedHashMap<Class<? extends Item>, ItemInfo> ITEMS_BY_CLASS_CACHE = new LinkedHashMap<>();


    private ItemsRegistry() {
        registerBuilder(Identifier.of("base:item/missing_no"), MissingNoItem.class, MissingNoItem.CODEC)
                .markAsHidden()
                .apply();
    }

    public void appendRegistryFromJson(final String read) throws Exception {
        final JSONObject itemsObject = new JSONObject(read).getJSONObject("items");
        final Iterator<String> iterator = itemsObject.keys();
        while (iterator.hasNext()) {
            final var idStr = iterator.next();
            final var id = Identifier.of(idStr);
            final var jsonObject = itemsObject.getJSONObject(idStr);

            _addItem(id, jsonObject);
        }
    }

    private void _addItem(final Identifier id, final JSONObject jsonObject) throws Exception {
        final AbstractItemCodec codec = (AbstractItemCodec) _getFieldReferenceValue(jsonObject.getString("codec"));
        final ItemFactory<?> factory = (ItemFactory<?>) _getFieldReferenceValue(jsonObject.getString("factory"));

        RegisterBuilder builder = registerBuilder(id, (Class<? extends Item>) Class.forName(jsonObject.getString("class")), codec)
                .setFactory(factory);

        if (jsonObject.has("inherits")) {
            final JSONArray inherits = jsonObject.getJSONArray("inherits");
            builder.setParents(CherryOrchard.parseStringArray(CherryOrchard.of(inherits), new String[0]));
        }

        if (jsonObject.has("required_feature_flag")) {
            builder.setRequiredFeatureFlag(FeatureFlag.valueOf(jsonObject.getString("required_feature_flag")));
        }

        if (jsonObject.has("hidden") && jsonObject.optBoolean("hidden", false)) {
            builder.markAsHidden();
        }

        builder.apply();
    }

    private Object _getFieldReferenceValue(String ref) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        String cls = ref.substring(0, ref.lastIndexOf("."));
        String fld = ref.substring(ref.lastIndexOf(".") + 1);

        return Class.forName(cls).getField(fld).get(null);
    }

    private RegisterBuilder registerBuilder(final Identifier identifier,
                                            final Class<? extends Item> clazzType,
                                            final AbstractItemCodec codec) {
        return new RegisterBuilder(identifier, clazzType, codec);
    }

    public Item debugCreateRandomIdem() {
        return new CheckboxItem(String.valueOf(RandomUtil.nextIntPositive()), RandomUtil.nextBoolean());
    }

    public class RegisterBuilder {

        @NotNull private final Identifier identifier;
        @NotNull private final Class<? extends Item> javaType;
        @NotNull private final AbstractItemCodec codec;
        private String[] parents;
        private FeatureFlag featureFlag;
        private ItemFactory<?> factory;
        private boolean hidden;

        private RegisterBuilder(@NonNull Identifier identifier,
                               @NonNull Class<? extends Item> javaType,
                               @NonNull AbstractItemCodec codec) {
            this.identifier = identifier;
            this.javaType = javaType;
            this.codec = codec;
        }

        public void apply() {
            if (getByKey(identifier) != null || getByKey(javaType) != null) {
                throw new RuntimeException("This content already registered!");
            }

            final List<ItemInfo> parents = new ArrayList<>();
            if (this.parents != null) {
                for (final String parent : this.parents) {
                    final ItemInfo p = REGISTRY.getByKey(Identifier.of(parent));
                    parents.add(p);
                }
            }
            final ItemInfo itemInfo = new ItemInfo(identifier,
                    javaType,
                    codec,
                    factory,
                    parents.toArray(new ItemInfo[0]));
            if (factory == null || hidden) {
                itemInfo.setNoAvailableToCreate();
            }
            if (featureFlag != null) {
                itemInfo.setRequiredFeatureFlag(featureFlag);
            }
            ITEMS.put(identifier, itemInfo);
            ITEMS_BY_CLASS_CACHE.put(javaType, itemInfo);
        }

        public RegisterBuilder setParents(String... s) {
            this.parents = s;
            return this;
        }

        public RegisterBuilder setRequiredFeatureFlag(FeatureFlag featureFlag) {
            this.featureFlag = featureFlag;
            return this;
        }

        public RegisterBuilder setFactory(ItemFactory<?> factory) {
            this.factory = factory;
            return this;
        }

        public RegisterBuilder markAsHidden() {
            this.hidden = true;
            return this;
        }
    }

    @NonNull
    public ItemInfo[] getAllItems() {
        return ITEMS.values().toArray(new ItemInfo[0]);
    }

    public int count() {
        return ITEMS.size();
    }

    @Nullable
    public ItemInfo getByKey(@NonNull Class<? extends Item> classType) {
        return ITEMS_BY_CLASS_CACHE.getOrDefault(classType, null);
    }

    @Nullable
    public ItemInfo getByKey(@NonNull String identifierString) {
        return ITEMS.getOrDefault(Identifier.of(identifierString), null);
    }

    @Override
    @Nullable
    public ItemInfo getByKey(@NonNull Identifier identifier) {
        return ITEMS.getOrDefault(identifier, null);
    }

    public static class ItemInfo {
        private final Identifier identifier;
        private final Class<? extends Item> classType;
        private final AbstractItemCodec codec;
        private final ItemFactory<?> factory;
        private final ItemInfo[] parents;
        private final Set<Identifier> cachedParentsIdentifiers = new HashSet<>();

        private boolean noAvailableToCreate;
        private FeatureFlag requiredFeatureFlag;

        public ItemInfo(@NotNull final Identifier identifier,
                        @NonNull final Class<? extends Item> classType,
                        @NonNull final AbstractItemCodec codec,
                        @Nullable final ItemFactory<?> factory,
                        @NotNull ItemInfo[] parents) {
            this.identifier = identifier;
            this.classType = classType;
            this.codec = codec;
            this.factory = factory;
            this.parents = parents;
            // cache parents
            for (ItemInfo parent : this.parents) {
                cachedParentsIdentifiers.add(parent.getIdentifier());
            }
        }

        public boolean isInherit(Identifier identifier) {
            if (this.identifier.equals(identifier)) return true;
            return cachedParentsIdentifiers.contains(identifier);
        }

        @NonNull
        public Item create() {
            if (noAvailableToCreate || factory == null) {
                throw new UnsupportedOperationException("ItemType " + identifier + " not supported creating");
            }
            return factory.create();
        }

        private void setRequiredFeatureFlag(FeatureFlag flag) {
            this.requiredFeatureFlag = flag;
        }

        private void setNoAvailableToCreate() {
            this.noAvailableToCreate = true;
        }

        public boolean isCompatibility(List<FeatureFlag> flags) {
            return isCompatibility(flags.toArray(new FeatureFlag[0]));
        }

        public boolean isCompatibility(FeatureFlag[] flags) {
            if (noAvailableToCreate) return false;
            if (requiredFeatureFlag == null) return true;
            for (FeatureFlag f : flags) {
                if (f == requiredFeatureFlag) {
                    return true;
                }
            }
            return false;
        }

        @NonNull
        public Class<? extends Item> getClassType() {
            return classType;
        }

        @NonNull
        public AbstractItemCodec getCodec() {
            return codec;
        }

        public ItemFactory<?> getFactory() {
            return factory;
        }

        public Identifier getIdentifier() {
            return identifier;
        }
    }
}
