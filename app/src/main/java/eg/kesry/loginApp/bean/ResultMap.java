package eg.kesry.loginApp.bean;

public class ResultMap<T> {

    private T data;

    private String message;

    private Integer errorCode;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ResultMap [data=" + data + ", message=" + message + ", errorCode=" + errorCode + "]";
    }

}
