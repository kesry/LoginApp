package eg.kesry.loginApp.callback;

import java.io.IOException;
import java.util.Map;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.Call;
import android.widget.Toast;

import eg.kesry.loginApp.MainActivity;
import eg.kesry.loginApp.bean.LoginServer;
import eg.kesry.loginApp.bean.ResultMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class LoginCb implements Callback {

    private MainActivity activity;

    private LoginServer server;

    public LoginCb(MainActivity activity) {
        this.activity = activity;
    }

    public LoginServer getServer() {
        return server;
    }

    public void setServer(LoginServer server) {
        this.server = server;
    }

    public void onFailure(Call call, IOException e) {
        activity.runOnUiThread(() -> activity.makeText("请求失败: " + e.getMessage(), Toast.LENGTH_SHORT));
    }

    public void onResponse(Call call, Response response) throws IOException {

        server.setMobileToken("login failed");
        server.setServerStatus(0);
        if (response.isSuccessful()) {
            String responseBody = response.body().string();                        
            ResultMap<Map<String, Object>> respBody = activity.getJsonMapper().readValue(responseBody,
                    new TypeReference<ResultMap<Map<String, Object>>>() {
            });
            int errorCode = respBody.getErrorCode();
            Map<String, Object> oData = respBody.getData();
            activity.runOnUiThread(() -> {
                if (errorCode == 0) {
                    try {
                        String mobileToken = oData.get("mobile_token").toString();
                        server.setMobileToken(mobileToken);
                        server.setServerStatus(1);
                        activity.makeText("登录成功", Toast.LENGTH_SHORT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        activity.makeText("登录失败：" + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                } else {
                    activity.makeText("账号或者密码错误。", Toast.LENGTH_SHORT);
                }
                activity.syncServers();
            });
        } else {
            activity.runOnUiThread(() -> activity.makeText("登录失败: " + response.code(), Toast.LENGTH_SHORT));
        }
    }
}
