package dev.architectury.buildsrc;

public class Reference<T> {
    private T value;
    public Reference() {
    
    }
    
    public Reference(T defaultValue) {
        this.value = defaultValue;
    }
    
    public T get() {
        return value;
    }
    
    public void set(T value) {
        this.value = value;
    }
}
