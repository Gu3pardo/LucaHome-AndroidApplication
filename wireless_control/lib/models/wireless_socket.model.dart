import 'entity.model.dart';

class WirelessSocket implements Entity {
  int id;
  String name;
  String code;
  String area;
  int state = 0;
  String description;
  String icon;
  int deletable;
  int lastToggled;
  String group;

  WirelessSocket(
      {this.id = -1,
      this.name = "",
      this.code = "",
      this.area = "",
      this.state = 0,
      this.description = "",
      this.icon = "fas fa-lightbulb",
      this.deletable = 1,
      this.lastToggled = 0,
      this.group = ""});

  WirelessSocket copyWith(
      {int id,
      String name,
      String code,
      String area,
      int state,
      String description,
      String icon,
      int deletable,
      int lastToggled,
      String group}) {
    return new WirelessSocket(
      id: id ?? this.id,
      name: name ?? this.name,
      code: code ?? this.code,
      area: area ?? this.area,
      state: state ?? this.state,
      description: description ?? this.description,
      icon: icon ?? this.icon,
      deletable: deletable ?? this.deletable,
      lastToggled: lastToggled ?? this.lastToggled,
      group: group ?? this.group,
    );
  }

  WirelessSocket.fromJson(Map<String, dynamic> json)
      : id = int.parse(json["id"]),
        name = json["name"],
        code = json["code"],
        area = json["area"],
        state = int.parse(json["state"]),
        description = json["description"],
        icon = json["icon"],
        deletable = int.parse(json["deletable"]),
        lastToggled = int.parse(json["lastToggled"]),
        group = json["group"];

  WirelessSocket.fromMap(Map<String, dynamic> map)
      : id = map["id"],
        name = map["name"],
        code = map["code"],
        area = map["area"],
        state = map["state"],
        description = map["description"],
        icon = map["icon"],
        deletable = map["deletable"],
        lastToggled = map["lastToggled"],
        group = map["group"];

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "code": code,
        "area": area,
        "state": state,
        "description": description,
        "icon": icon,
        "deletable": deletable,
        "lastToggled": lastToggled,
        "group": group,
      };

  Map<String, dynamic> toAddJson() => {
        "name": name,
        "code": code,
        "area": area,
        "state": state,
        "description": description,
        "icon": icon,
        "deletable": deletable,
        "lastToggled": lastToggled,
        "group": group,
      };

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
          deletable == other.deletable &&
          lastToggled == other.lastToggled &&
          group == other.group;

  @override
  int get hashCode =>
      id.hashCode ^
      name.hashCode ^
      code.hashCode ^
      area.hashCode ^
      state.hashCode ^
      description.hashCode ^
      icon.hashCode ^
      deletable.hashCode ^
      lastToggled.hashCode ^
      group.hashCode;

  @override
  String toString() {
    return 'WirelessSocket{id: $id, name: $name, code: $code, area: $area, state: $state, description: $description, icon: $icon, deletable: $deletable}, lastToggled: $lastToggled}, group: $group}';
  }
}
