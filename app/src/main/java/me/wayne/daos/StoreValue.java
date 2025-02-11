package me.wayne.daos;

import javax.annotation.Nonnull;

public class StoreValue {

    @Nonnull
    private final Object value;

    @Nonnull
    public <T> T getValue(Class<T> clazz) {
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("Value is not of the expected type (" + clazz.getSimpleName() + ")");
        }
        return clazz.cast(value);
    }

    public Object getValue() {
        return value;
    }

    public boolean isInstanceOfClass(Class<?> clazz) {
        return clazz.isInstance(value);
    }

    public StoreValue(@Nonnull Object value) {
        this.value = value;
    }
    
}
