package eg.kesry.loginApp.bean;

public class LoginServer {

    private String serverUrl;

    private String username;

    private String password;

    private String hashString; // 对serverUrl进行hash运算

    private String mobileToken;

    private Integer serverStatus; // 显示服务登录状态

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHashString() {
        return hashString;
    }

    public void setHashString(String hashString) {
        this.hashString = hashString;
    }

    public String getMobileToken() {
        return mobileToken;
    }

    public void setMobileToken(String mobileToken) {
        this.mobileToken = mobileToken;
    }

    public Integer getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(Integer serverStatus) {
        this.serverStatus = serverStatus;
    }

    @Override
    public String toString() {
        return "LoginServer [serverUrl=" + serverUrl + ", username=" + username + ", password=" + password
                + ", hashString=" + hashString + ", mobileToken=" + mobileToken + ", serverStatus=" + serverStatus
                + "]";
    }

}
