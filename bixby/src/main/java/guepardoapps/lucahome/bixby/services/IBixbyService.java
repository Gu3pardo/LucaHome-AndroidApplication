package guepardoapps.lucahome.bixby.services;

@SuppressWarnings({"unused"})
public interface IBixbyService {
    String BixbyAvailabilityBroadcast = "guepardoapps.lucahome.bixby.services.BixbyService.Availability.Bixby";
    String BixbyAvailabilityBundle = "BixbyAvailabilityBundle";

    long GetMaxRunFrequencyMs();

    void SetMaxRunFrequencyMs(long maxRunFrequencyMs);

    void CheckIfBixbyIsAvailable();

    boolean BixbyServiceIsAvailable();
}
