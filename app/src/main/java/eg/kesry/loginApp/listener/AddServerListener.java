package eg.kesry.loginApp.listener;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import eg.kesry.loginApp.MainActivity;
import eg.kesry.loginApp.R;
import eg.kesry.loginApp.utils.SecurityUtil;

import eg.kesry.loginApp.bean.LoginServer;


// 添加按钮使用的监听器
public class AddServerListener implements View.OnClickListener {

    private MainActivity activity;

    private LayoutInflater inflater;

    public AddServerListener(MainActivity activity, LayoutInflater inflater) {
        this.activity = activity;
        this.inflater = inflater;
    }

    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = inflater.inflate(R.layout.dialog_add_server, null);

        EditText etServerHost = dialogView.findViewById(R.id.server_host);
        EditText etServerLoginName = dialogView.findViewById(R.id.server_login_name);
        EditText etServerLoginPwd = dialogView.findViewById(R.id.server_login_pwd);

        builder.setView(dialogView)
                .setTitle("新增服务信息")
                .setPositiveButton("登录", (dialog, which) -> {

                    String serverHost = etServerHost.getText().toString();
                    String username = etServerLoginName.getText().toString();
                    String password = etServerLoginPwd.getText().toString();
                    if (serverHost.isEmpty() || username.isEmpty() || password.isEmpty()) {
                        activity.makeText("必填项不能为空", Toast.LENGTH_SHORT);
                        return;
                    }
                    String hashString = SecurityUtil.md5HexString(serverHost + username);
                    // 封装服务信息
                    LoginServer server = new LoginServer();
                    server.setServerUrl(serverHost);
                    server.setUsername(username);
                    server.setPassword(password);
                    server.setHashString(hashString);
                    // 保存到服务器列表
                    activity.saveLoginServer(server);
                    // 保存到文件
                    activity.syncServers();
                    // 登录到服务器
                    activity.login(server);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

}
