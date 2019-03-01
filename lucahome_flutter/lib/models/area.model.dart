class Area {
  int id;
  String name;
  String filter;
  int deletable;

  Area({
    this.id = -1,
    this.name = "",
    this.filter = "",
    this.deletable = 1,
  });

  Area copyWith({int id, String name, String filter, int deletable}) {
    return new Area(
      id: id ?? this.id,
      name: name ?? this.name,
      filter: filter ?? this.filter,
      deletable: deletable ?? this.deletable,
    );
  }

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Area &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          name == other.name &&
          filter == other.filter &&
          deletable == other.deletable;

  @override
  int get hashCode => id.hashCode ^ name.hashCode ^ filter.hashCode ^ deletable.hashCode;

  @override
  String toString() {
    return 'Area{id: $id, {name: $name, filter: $filter, deletable: $deletable}';
  }
}
