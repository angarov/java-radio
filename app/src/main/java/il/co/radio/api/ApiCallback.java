package il.co.radio.api;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception error);
}
