package entities;

public class RateLimit {
    long resetTime;
    int remainingRequest;
    int totalRequest;
    int usedRequest;

    public RateLimit(long resetTime, int remainingRequest, int totalRequest, int usedRequest) {
        this.resetTime = resetTime;
        this.remainingRequest = remainingRequest;
        this.totalRequest = totalRequest;
        this.usedRequest = usedRequest;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(int resetTime) {
        this.resetTime = resetTime;
    }

    public int getRemainingRequest() {
        return remainingRequest;
    }

    public void setRemainingRequest(int remainingRequest) {
        this.remainingRequest = remainingRequest;
    }

    public int getTotalRequest() {
        return totalRequest;
    }

    public void setTotalRequest(int totalRequest) {
        this.totalRequest = totalRequest;
    }

    public int getUsedRequest() {
        return usedRequest;
    }

    public void setUsedRequest(int usedRequest) {
        this.usedRequest = usedRequest;
    }
}
