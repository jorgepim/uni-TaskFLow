package sv.edu.catolica.taskflow.models;

public class ApiResponse<T> {
    private String status;
    private String message;
    private String timestamp;
    private T data;

    public ApiResponse() {}

    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public boolean isSuccessful() {
        return "success".equals(status);
    }
}
