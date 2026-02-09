package eg.kesry.loginApp.callback;

import java.io.IOException;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.Call;

import android.widget.Toast;

import eg.kesry.loginApp.MainActivity;
import eg.kesry.loginApp.bean.ResultMap;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;


import java.util.Map;
public class ConfirmLoginCb implements Callback {

    private MainActivity activity;

    private String uuid;

    private JsonMapper jsonMapper;

    public ConfirmLoginCb(MainActivity activity, String uuid) {
        this.activity = activity;
        this.jsonMapper = activity.getJsonMapper();
        this.uuid = uuid;
    }

    public void onFailure(Call call, IOException e) {
        activity.runOnUiThread(() -> activity.makeText("请求失败: " + e.getMessage(), Toast.LENGTH_SHORT));
    }

    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            // 进行确认登录操作
            String responseBody = response.body().string();
            ResultMap<Map<String, Object>> respBody = jsonMapper.readValue(responseBody,
                    new TypeReference<ResultMap<Map<String, Object>>>() {
                    });
            if (respBody.getErrorCode() == 0) {
                activity.runOnUiThread(() -> activity.makeText("登录成功", Toast.LENGTH_SHORT));
            } else {
                activity.runOnUiThread(() -> activity
                        .makeText("扫码失败: " + respBody.getMessage(), Toast.LENGTH_SHORT));
            }

        } else {
            activity.runOnUiThread(() -> activity
                    .makeText("扫码失败: " + response.code(), Toast.LENGTH_SHORT));
        }
    }

}
