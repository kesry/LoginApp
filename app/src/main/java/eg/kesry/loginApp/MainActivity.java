package eg.kesry.loginApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.fasterxml.jackson.core.type.TypeReference;
import eg.kesry.loginApp.adapter.ServerListAdapter;
import eg.kesry.loginApp.bean.LoginServer;
import eg.kesry.loginApp.bean.ResultMap;
import eg.kesry.loginApp.utils.SecurityUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.fasterxml.jackson.databind.json.JsonMapper;
import eg.kesry.loginApp.listener.AddServerListener;
import eg.kesry.loginApp.callback.LoginCb;
import eg.kesry.loginApp.callback.ScanCb;
import eg.kesry.loginApp.callback.ConfirmLoginCb;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;

public class MainActivity extends AppCompatActivity {

    private ImageButton actionButton;
    private ImageButton updateButton; // 新增更新按钮

    private OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();

    private JsonMapper jsonMapper = new JsonMapper();;

    private Map<String, LoginServer> servers;

    private List<LoginServer> serverList;

    private ListView listView;

    private ServerListAdapter serverListAdapter;

    private LoginServer currentChooseServer;

    private AddServerListener addServerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // 初始化数据信息
        this.servers = getAllLoginServers();
        this.serverList = new ArrayList<>(this.servers.values());

        // 初始化监听器
        addServerListener = new AddServerListener(this, getLayoutInflater());

        // 初始化组件信息
        actionButton = findViewById(R.id.action_add);
        updateButton = findViewById(R.id.btn_home); // 初始化更新按钮

        serverListAdapter = new ServerListAdapter(this, serverList);
        listView = findViewById(R.id.list_view);
        listView.setAdapter(serverListAdapter);
        serverListAdapter.notifyDataSetChanged();

        actionButton.setOnClickListener(addServerListener);

        // 设置更新按钮点击事件
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UpdateCheckActivity.class);
                startActivity(intent);
            }
        });
    }

    private Map<String, LoginServer> getAllLoginServers() {

        if (this.servers != null) {
            return this.servers;
        }

        // 读取现有数据
        Map<String, LoginServer> servers = new LinkedHashMap<>();
        try {
            File file = new File(getFilesDir(), "servers.json");
            if (file.exists()) {
                String jsonString = readAllBytesCompatible(file);
                servers = jsonMapper.readValue(jsonString, new TypeReference<LinkedHashMap<String, LoginServer>>() {
                });
            } else {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 调试的时候打开
            // Toast.makeText(this, "读取数据失败 " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return servers;

    }

    // 保存数据
    public void syncServers() {
        try {
            // 获取文件路径
            // 添加新的服务器信息
            // 写入文件
            File file = new File(getFilesDir(), "servers.json");
            String jsonString = jsonMapper.writeValueAsString(servers);

            // 使用传统IO方式写入文件，避免API级别问题
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存数据失败 " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void saveLoginServer(LoginServer server) {

        servers.put(server.getHashString(), server);
        serverList.add(server);
        serverListAdapter.notifyDataSetChanged();

    }

    public void login(LoginServer server) {

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String formData = "username=" + server.getUsername() + "&password=" + server.getPassword();

        RequestBody body = RequestBody.create(formData, mediaType);
        Request request = new Request.Builder()
                .url(HttpUrl.get(server.getServerUrl() + "/login").newBuilder()
                        .build()
                        .toString())
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("user-agent", "kdocs mobile client")
                .build();

        // 设置回调
        LoginCb loginCb = new LoginCb(this);
        loginCb.setServer(server);
        try {
            client.newCall(request).enqueue(loginCb);
        } catch (Exception e) {
            Toast.makeText(this, "请求失败: " + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // 添加兼容方法
    private String readAllBytesCompatible(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return new String(bos.toByteArray());
    }

    public void scan(LoginServer server) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setPrompt("扫描二维码");
        integrator.setOrientationLocked(false);
        integrator.setCameraId(0); // 使用默认的相机
        integrator.setBeepEnabled(false); // 扫到码后播放提示音
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
        this.currentChooseServer = server;
    }

    /**
    * 扫码结果事件
    * @param requestCode
    * @param resultCode
    * @param data
    */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "扫码取消！", Toast.LENGTH_LONG).show();
            } else {
                String uuid = result.getContents();
                setScanned(uuid);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 设置为已扫码

    private void setScanned(String uuid) {

        String invokeUrl = HttpUrl.get(this.currentChooseServer.getServerUrl() + "/qrcode/" + uuid).newBuilder()
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(invokeUrl)
                .get()
                .addHeader("user-agent", "kdocs mobile client")
                .addHeader("Authorization", "Bearer " + this.currentChooseServer.getMobileToken())
                .build();
        ScanCb scanCb = new ScanCb(this, uuid);
        try {

            client.newCall(request).enqueue(scanCb);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void confirmLogin(String uuid) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        String invokeUrl = HttpUrl.get(this.currentChooseServer.getServerUrl() + "/confirm/qrcode").newBuilder()
                .build()
                .toString();

        RequestBody body = RequestBody.create("uuid=" + uuid, mediaType);

        Request request = new Request.Builder()
                .url(invokeUrl)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("user-agent", "kdocs mobile client")
                .addHeader("authorization", this.currentChooseServer.getMobileToken())
                .build();
        try {

            ConfirmLoginCb confirmLoginCb = new ConfirmLoginCb(this, uuid);
            client.newCall(request).enqueue(confirmLoginCb);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void makeText(Object msg, int duration) {
        Toast.makeText(this, "" + msg, duration).show();
    }

    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public void removeServer(LoginServer server) {
        servers.remove(server.getHashString());
        Iterator<LoginServer> iterator = serverList.iterator();
        while (iterator.hasNext()) {
            LoginServer loginServer = iterator.next();
            if (loginServer.getHashString().equals(server.getHashString())) {
                iterator.remove();
                break;
            }
        }
        serverListAdapter.notifyDataSetChanged();
    }

    public void editServer(LoginServer server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_server, null);

        EditText etServerHost = dialogView.findViewById(R.id.server_host);
        EditText etServerLoginName = dialogView.findViewById(R.id.server_login_name);
        EditText etServerLoginPwd = dialogView.findViewById(R.id.server_login_pwd);

        // 填充当前服务器信息
        etServerHost.setText(server.getServerUrl());
        etServerLoginName.setText(server.getUsername());
        etServerLoginPwd.setText(server.getPassword());

        builder.setView(dialogView)
                .setTitle("编辑服务信息")
                .setPositiveButton("保存并重新登录", (dialog, which) -> {
                    String serverHost = etServerHost.getText().toString();
                    String username = etServerLoginName.getText().toString();
                    String password = etServerLoginPwd.getText().toString();

                    if (serverHost.isEmpty() || username.isEmpty() || password.isEmpty()) {
                        makeText("必填项不能为空", Toast.LENGTH_SHORT);
                        return;
                    }

                    String oldHashString = server.getHashString();
                    String newHashString = SecurityUtil.md5HexString(serverHost + username);

                    // 创建新的服务器对象以确保数据一致性
                    LoginServer updatedServer = new LoginServer();
                    updatedServer.setServerUrl(serverHost);
                    updatedServer.setUsername(username);
                    updatedServer.setPassword(password);
                    updatedServer.setHashString(newHashString);
                    // 保留原有的mobileToken
                    updatedServer.setMobileToken(server.getMobileToken());
                    removeServer(server);
                    // 删除之后，同步到文件
                    syncServers();
                    // serverListAdapter.notifyDataSetChanged();
                    // 在登陆前保存一遍文件
                    saveLoginServer(updatedServer);
                    // 自动重新登录
                    login(updatedServer);

                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}