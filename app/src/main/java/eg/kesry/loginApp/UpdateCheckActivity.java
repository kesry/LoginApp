package eg.kesry.loginApp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.Callback;
import okhttp3.Call;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class UpdateCheckActivity extends AppCompatActivity {

    private TextView currentVersionText;
    private Button checkUpdateButton;
    private TextView updateInfoText;

    private String currentVersion;
    

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_check);

        // 初始化组件
        currentVersionText = findViewById(R.id.current_version_text);
        checkUpdateButton = findViewById(R.id.check_update_button);
        updateInfoText = findViewById(R.id.update_info_text);

        // 显示当前版本号
        displayCurrentVersion();

        // 设置检查更新按钮点击事件
        checkUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForUpdates();
            }
        });
    }

    private void displayCurrentVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            currentVersion = "v" + versionName;
            
            String versionInfo = "当前版本: " + versionName + " (版本号: " + versionCode + ")";
            currentVersionText.setText(versionInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            currentVersionText.setText("无法获取版本信息");
        }
    }

    private void checkForUpdates() {
        updateInfoText.setText("正在检查更新...");
        checkUpdateButton.setEnabled(false);
        
        // 获取最新版本
        getLatestVersion();


    }

    private void showUpdateAvailable(String version) {
        String updateInfo = "发现新版本: " + version;
        
        updateInfoText.setText(updateInfo);
        Toast.makeText(this, "发现新版本，建议更新", Toast.LENGTH_LONG).show();

        // 将检查更新按钮改为立即更新按钮
        checkUpdateButton.setText("立即更新");
        checkUpdateButton.setEnabled(true);
        
        // 为更新按钮添加点击事件
        checkUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performUpdate(version);
            }
        });
    }

    private void showNoUpdate() {
        updateInfoText.setText("当前已是最新版本");
        Toast.makeText(this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
        checkUpdateButton.setEnabled(true);
    }

    private void dealVersionInfo(String version) { 
        // 移除版本字符串前后的空白字符进行比较
        String trimmedRemoteVersion = version.trim();
        String trimmedCurrentVersion = this.currentVersion.trim();
        
        if (Objects.equals(trimmedRemoteVersion, trimmedCurrentVersion)) {
            showNoUpdate();
        } else {
            showUpdateAvailable(version);
        }
    }


    // 最新的安装包版本
    // https://github.com/kesry/LoginApp/raw/refs/heads/main/VERSION.txt 返回类似v1.0 这种版本信息

    private void getLatestVersion() {
        String invokeUrl = "https://github.com/kesry/LoginApp/raw/refs/heads/main/VERSION.txt";

        Request request = new Request.Builder()
            .url(invokeUrl)
            .get()
            .build();
        try {
             client.newCall(request).enqueue(new Callback() { 
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(UpdateCheckActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                public void onResponse(Call call, Response response) throws IOException{
                    if (response.isSuccessful()) {
                        String version = response.body().string();
                        runOnUiThread(() -> {
                            dealVersionInfo(version);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(UpdateCheckActivity.this, "检查更新失败: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
             });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(UpdateCheckActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    
    private void performUpdate(String version) {
        updateInfoText.setText("正在准备更新...");
        checkUpdateButton.setEnabled(false);
        checkUpdateButton.setText("更新中...");
        
        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                updateInfoText.setText("请授予存储权限以继续更新");
                checkUpdateButton.setEnabled(true);
                checkUpdateButton.setText("立即更新");
                return;
            }
        }

        // 下载APK文件
        downloadApk(version);
    }

    private void downloadApk(String version) {
        updateInfoText.setText("正在下载更新包...");

        // 构建下载URL
        String apkUrl = "https://github.com/kesry/LoginApp/releases/download/" + version + "/app-release.apk";
        
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("LoginApp更新");
        request.setDescription("正在下载版本 " + version);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LoginApp-" + version + ".apk");
        
        // 获取DownloadManager实例
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);
        
        // 注册广播接收器监听下载完成
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long receivedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == receivedDownloadId) {
                    installApk(downloadManager, downloadId);
                    unregisterReceiver(this);
                }
            }
        };
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        
        updateInfoText.setText("开始下载更新包，请稍候...");
    }

    private void installApk(DownloadManager downloadManager, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        
        if (cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(statusIndex);
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String downloadedFileUri = cursor.getString(uriIndex);
                
                if (downloadedFileUri != null) {
                    Uri apkUri = Uri.parse(downloadedFileUri);
                    
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    updateInfoText.setText("下载完成，准备安装...");
                    startActivity(installIntent);
                }
            } else {
                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(reasonIndex);
                updateInfoText.setText("下载失败，错误代码: " + reason);
                checkUpdateButton.setEnabled(true);
                checkUpdateButton.setText("重新下载");
                checkUpdateButton.setOnClickListener(v -> checkForUpdates());
            }
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，重新尝试更新
                checkForUpdates();
            } else {
                updateInfoText.setText("存储权限被拒绝，无法进行更新");
                checkUpdateButton.setEnabled(true);
                checkUpdateButton.setText("立即更新");
            }
        }
    }
}