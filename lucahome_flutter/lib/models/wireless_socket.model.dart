class WirelessSocket {
  int id;
  String name;
  String code;
  String area;
  int state = 0;
  String description;
  String icon;
  int deletable;

  WirelessSocket({
    this.id = -1,
    this.name = "",
    this.code = "",
    this.area = "",
    this.state = 1,
    this.description = "",
    this.icon = "",
    this.deletable = 1,
  });

  WirelessSocket copyWith(
      {int id,
      String name,
      String code,
      String area,
      int state,
      String description,
      String icon,
      int deletable}) {
    return new WirelessSocket(
      id: id ?? this.id,
      name: name ?? this.name,
      code: code ?? this.code,
      area: area ?? this.area,
      state: state ?? this.state,
      description: description ?? this.description,
      icon: icon ?? this.icon,
      deletable: deletable ?? this.deletable,
    );
  }

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is WirelessSocket &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          name == other.name &&
          code == other.code &&
          area == other.area &&
          state == other.state &&
          description == other.description &&
          icon == other.icon &&
          deletable == other.deletable;

  @override
  int get hashCode =>
      id.hashCode ^
      name.hashCode ^
      code.hashCode ^
      area.hashCode ^
      state.hashCode ^
      description.hashCode ^
      icon.hashCode ^
      deletable.hashCode;

  @override
  String toString() {
    return 'WirelessSocket{id: $id, name: $name, code: $code, area: $area, state: $state, description: $description, icon: $icon, deletable: $deletable}';
  }
}
