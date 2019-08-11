/// Returns the first element that satisfies the given predicate [test].
/// Iterates through elements and returns the first to satisfy [test].
/// If no element satisfies [test], the result of invoking the [orElse]
/// function is returned.
/// If [orElse] is omitted, it defaults to returning [null].
E firstOrNullWhere<E>(Iterable<E> iterable, bool test(E element), {E orElse()}) {
  for (E element in iterable) {
    if (test(element)) return element;
  }
  if (orElse != null) return orElse();
  return null;
}
