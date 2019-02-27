class Area {
  String name;
  String filter;
  int deletable;

  Area({
    this.name = "",
    this.filter = "",
    this.deletable = 1,
  });

  Area copyWith({String name, String filter, int deletable}) {
    return new Area(
      name: name ?? this.name,
      filter: filter ?? this.filter,
      deletable: deletable ?? this.deletable,
    );
  }

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Area &&
          runtimeType == other.runtimeType &&
          name == other.name &&
          filter == other.filter &&
          deletable == other.deletable;

  @override
  int get hashCode => name.hashCode ^ filter.hashCode ^ deletable.hashCode;

  @override
  String toString() {
    return 'Area{name: $name, filter: $filter, deletable: $deletable}';
  }
}
