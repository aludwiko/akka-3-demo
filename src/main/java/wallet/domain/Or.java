package wallet.domain;

public sealed interface Or<E, T> {

  record Left<E, T>(E error) implements Or<E, T> {
  }

  record Right<E, T>(T value) implements Or<E, T> {
  }

  static <E, T> Left<E, T> left(E error) {
    return new Left<>(error);
  }

  static <E, T> Right<E, T> right(T value) {
    return new Right<>(value);
  }

}
