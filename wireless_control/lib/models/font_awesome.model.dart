// Based on https://github.com/brianegan/font_awesome_flutter/blob/master/example/lib/example_icon.dart

import 'package:flutter/widgets.dart';

class FontAwesome implements Comparable {
  final IconData value;
  final String key;

  FontAwesome(this.value, this.key);

  @override
  String toString() => 'FontAwesome{value: $value, key: $key}';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FontAwesome &&
          runtimeType == other.runtimeType &&
          value == other.value &&
          key == other.key;

  @override
  int get hashCode => value.hashCode ^ key.hashCode;

  @override
  int compareTo(other) => key.compareTo(other.key);
}
