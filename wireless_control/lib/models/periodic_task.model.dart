import 'entity.model.dart';

class PeriodicTask implements Entity {
  int id;
  String name;
  int wirelessSocketId;
  int wirelessSocketState;
  int weekday;
  int hour;
  int minute;
  int periodic;
  int active;

  PeriodicTask({
    this.id = -1,
    this.name = "",
    this.wirelessSocketId = -1,
    this.wirelessSocketState = 1,
    this.weekday = 1,
    this.hour = 0,
    this.minute = 0,
    this.periodic = 1,
    this.active = 1,
  });

  PeriodicTask copyWith(
      {int id,
      String name,
      int wirelessSocketId,
      int wirelessSocketState,
      int weekday,
      int hour,
      int minute,
      int periodic,
      int active}) {
    return new PeriodicTask(
      id: id ?? this.id,
      name: name ?? this.name,
      wirelessSocketId: wirelessSocketId ?? this.wirelessSocketId,
      wirelessSocketState: wirelessSocketState ?? this.wirelessSocketState,
      weekday: weekday ?? this.weekday,
      hour: hour ?? this.hour,
      minute: minute ?? this.minute,
      periodic: periodic ?? this.periodic,
      active: active ?? this.active,
    );
  }

  PeriodicTask.fromJson(Map<String, dynamic> json)
      : id = int.parse(json["id"]),
        name = json["name"],
        wirelessSocketId = int.parse(json["wirelessSocketId"]),
        wirelessSocketState = int.parse(json["wirelessSocketState"]),
        weekday = int.parse(json["weekday"]),
        hour = int.parse(json["hour"]),
        minute = int.parse(json["minute"]),
        periodic = int.parse(json["periodic"]),
        active = int.parse(json["active"]);

  PeriodicTask.fromMap(Map<String, dynamic> map)
      : id = map["id"],
        name = map["name"],
        wirelessSocketId = map["wirelessSocketId"],
        wirelessSocketState = map["wirelessSocketState"],
        weekday = map["weekday"],
        hour = map["hour"],
        minute = map["minute"],
        periodic = map["periodic"],
        active = map["active"];

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "wirelessSocketId": wirelessSocketId,
        "wirelessSocketState": wirelessSocketState,
        "weekday": weekday,
        "hour": hour,
        "minute": minute,
        "periodic": periodic,
        "active": active,
      };

  Map<String, dynamic> toAddJson() => {
        "name": name,
        "wirelessSocketId": wirelessSocketId,
        "wirelessSocketState": wirelessSocketState,
        "weekday": weekday,
        "hour": hour,
        "minute": minute,
        "periodic": periodic,
        "active": active,
      };

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is PeriodicTask &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          name == other.name &&
          wirelessSocketId == other.wirelessSocketId &&
          wirelessSocketState == other.wirelessSocketState &&
          weekday == other.weekday &&
          hour == other.hour &&
          minute == other.minute &&
          periodic == other.periodic &&
          active == other.active;

  @override
  int get hashCode =>
      id.hashCode ^
      name.hashCode ^
      wirelessSocketId.hashCode ^
      wirelessSocketState.hashCode ^
      weekday.hashCode ^
      hour.hashCode ^
      minute.hashCode ^
      periodic.hashCode ^
      active.hashCode;

  @override
  String toString() {
    return 'PeriodicTask{id: $id, name: $name, wirelessSocketId: $wirelessSocketId, wirelessSocketState: $wirelessSocketState, weekday: $weekday, hour: $hour, minute: $minute, periodic: $periodic, active: $active}';
  }
}
