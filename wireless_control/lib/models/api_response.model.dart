class ApiResponseModel<T> {
  String status;
  String message;
  T data;

  ApiResponseModel({this.status = "", this.message = "", this.data});

  ApiResponseModel copyWith({String status, String message, T data}) {
    return new ApiResponseModel(
      status: status ?? this.status,
      message: message ?? this.message,
      data: data ?? this.data,
    );
  }

  ApiResponseModel.fromJson(Map<String, dynamic> json)
      : status = json["status"],
        message = json["message"],
        data = json["data"];

  Map<String, dynamic> toJson() => {
        "status": status,
        "message": message,
        "data": data,
      };

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ApiResponseModel &&
          runtimeType == other.runtimeType &&
          status == other.status &&
          message == other.message &&
          data == other.data;

  @override
  int get hashCode =>
      status.hashCode ^ message.hashCode ^ (data != null ? data.hashCode : 0);

  @override
  String toString() {
    return 'ApiResponseModel{status: $status, message: $message, data: $data}';
  }
}
