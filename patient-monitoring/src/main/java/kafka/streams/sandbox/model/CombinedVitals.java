package kafka.streams.sandbox.model;

public class CombinedVitals {
    private final Long heartRate;
    private final BodyTemp bodyTemp;

    public CombinedVitals(Long heartRate, BodyTemp bodyTemp) {
        this.heartRate = heartRate;
        this.bodyTemp = bodyTemp;
    }

    public Long getHeartRate() {
        return this.heartRate;
    }

    public BodyTemp getBodyTemp() {
        return this.bodyTemp;
    }

    @Override
    public String toString() {
        return "{" + " heartRate='" + getHeartRate() + "'" + ", bodyTemp='" + getBodyTemp() + "'" + "}";
    }
}
