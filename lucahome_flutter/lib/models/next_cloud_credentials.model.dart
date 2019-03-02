class NextCloudCredentials {
  String baseUrl;
  String userName;
  String passPhrase;

  NextCloudCredentials({
    this.baseUrl = "",
    this.userName = "",
    this.passPhrase = "",
  });

  NextCloudCredentials copyWith(
      {String baseUrl, String userName, String passPhrase}) {
    return new NextCloudCredentials(
      baseUrl: baseUrl ?? this.baseUrl,
      userName: userName ?? this.userName,
      passPhrase: passPhrase ?? this.passPhrase,
    );
  }

  bool hasServer() {
    return this.baseUrl != "";
  }

  bool isLoggedIn() {
    return this.userName != "" && this.passPhrase != "";
  }

  NextCloudCredentials.fromJson(Map<String, dynamic> json)
      : baseUrl = json["baseUrl"],
        userName = json["userName"],
        passPhrase = json["passPhrase"];

  Map<String, dynamic> toJson() => {
        "baseUrl": baseUrl,
        "userName": userName,
        "passPhrase": passPhrase,
      };

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is NextCloudCredentials &&
          runtimeType == other.runtimeType &&
          baseUrl == other.baseUrl &&
          userName == other.userName &&
          passPhrase == other.passPhrase;

  @override
  int get hashCode =>
      baseUrl.hashCode ^ userName.hashCode ^ passPhrase.hashCode;

  @override
  String toString() {
    return 'NextCloudCredentials{baseUrl: $baseUrl, userName: $userName, passPhrase: ********}';
  }
}
