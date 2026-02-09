package eg.kesry.loginApp.callback;

import java.io.IOException;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.Call;
import android.widget.Toast;

import eg.kesry.loginApp.MainActivity;


public class ScanCb implements Callback {

    private MainActivity activity;

    private String uuid;

    public ScanCb(MainActivity activity, String uuid) {
        this.activity = activity;
        this.uuid = uuid;
    }


    public void onFailure(Call call, IOException e) {
        activity.runOnUiThread(() -> activity.makeText("请求失败: " + e.getMessage(), Toast.LENGTH_SHORT));
    }

    public void onResponse(Call call, Response response) {
        if (response.isSuccessful()) {
            activity.runOnUiThread(() -> activity.confirmLogin(uuid));
        } else {
            activity.runOnUiThread(() -> activity.makeText("扫码失败: " + response.code(),  Toast.LENGTH_SHORT));
        }
    }

}
