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
import androidx.core.content.FileProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.Call;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class UpdateCheckActivity extends AppCompatActivity {

    private TextView currentVersionText;
    private Button checkUpdateButton;
    private TextView updateInfoText;

    private String currentVersion;
    private String pendingVersion;
    private static final int INSTALL_PERMISSION_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 3;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private long downloadId = -1;
    private DownloadManager downloadManager;
    
    // 下载完成广播接收器
    private BroadcastReceiver downloadCompleteReceiver;

    private OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_check);

        // 初始化组件
        currentVersionText = findViewById(R.id.current_version_text);
        checkUpdateButton = findViewById(R.id.check_update_button);
        updateInfoText = findViewById(R.id.update_info_text);

        // 初始化DownloadManager
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

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
        try {

            String[] remoteVersion = version.trim().split("\\.");
            String[] localVersion = this.currentVersion.trim().split("\\.");

            Integer bigVersionRe = Integer.valueOf(remoteVersion[0].substring(1));
            Integer bigVersionLe = Integer.valueOf(localVersion[0].substring(1));
            
            if (bigVersionLe < bigVersionRe) {
                showUpdateAvailable(version);
            } else if (bigVersionLe == bigVersionRe) {
                Integer smallVersionRe = Integer.valueOf(remoteVersion[1]); 
                Integer smallVersionLe = Integer.valueOf(localVersion[1]);
                if (smallVersionLe < smallVersionRe) {
                    showUpdateAvailable(version);
                } else {
                    showNoUpdate();
                }
            } else {
                showNoUpdate();
            }
        } catch (Exception e) {
            Toast.makeText(UpdateCheckActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            showNoUpdate();
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
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateCheckActivity.this, "检查更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        checkUpdateButton.setEnabled(true);
                        updateInfoText.setText("检查更新失败：" + e);
                    });
                }

                public void onResponse(Call call, Response response) throws IOException{
                    if (response.isSuccessful()) {
                        String version = response.body().string().trim();
                        runOnUiThread(() -> {
                            dealVersionInfo(version);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(UpdateCheckActivity.this, "检查更新失败: " + response.code(), Toast.LENGTH_SHORT).show());
                        checkUpdateButton.setEnabled(true);
                    }
                }
             });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "检查更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            checkUpdateButton.setEnabled(true);
        }

    }
    
    private void performUpdate(String version) {
        updateInfoText.setText("正在准备更新...");
        checkUpdateButton.setEnabled(false);
        checkUpdateButton.setText("更新中...");
        
        // 保存待处理的版本号
        pendingVersion = version;
        
        // 检查存储权限（适配不同Android版本）
        if (!checkAndRequestStoragePermissions()) {
            return;
        }
        
        // 检查安装权限
        if (!checkInstallPermission()) {
            return;
        }

        // 下载APK文件
        downloadApk(version);
    }
    
    /**
     * 检查并请求存储权限（针对APK下载优化）
     * @return 是否有权限
     */
    private boolean checkAndRequestStoragePermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        // Android 13+ (TIRAMISU) 及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 主要需要通知权限用于DownloadManager
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        } 
        // Android 10-12 (Q-S)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用分区存储，但仍需要读取权限来访问Download目录
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } 
        // Android 6.0-9 (M-P)
        else {
            // 传统外部存储权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsNeeded.toArray(new String[0]), STORAGE_PERMISSION_REQUEST_CODE);
            updateInfoText.setText("请授予权限以继续更新");
            checkUpdateButton.setEnabled(true);
            checkUpdateButton.setText("立即更新");
            return false;
        }
        return true;
    }
    
    /**
     * 检查安装未知应用权限
     * @return 是否有权限
     */
    private boolean checkInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                // 跳转到设置页面请求安装权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                updateInfoText.setText("请允许安装未知应用权限");
                checkUpdateButton.setEnabled(true);
                checkUpdateButton.setText("立即更新");
                return false;
            }
        }
        return true;
    }


    private void downloadApk(String version) {
        updateInfoText.setText("正在下载更新包...");

        // 构建下载URL
        String apkUrl = "https://github.com/kesry/LoginApp/releases/download/" + version + "/app-release.apk";
        
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setTitle("LoginApp更新");
            request.setDescription("正在下载版本 " + version);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            // 设置下载路径 - 使用应用私有目录更安全
            String fileName = "LoginApp-" + version + ".apk";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用Download目录
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            } else {
                // 较老版本使用应用私有目录
                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);
            }
            
            // 开始下载
            downloadId = downloadManager.enqueue(request);
            
            updateInfoText.setText("正在后台下载安装包。");
            
        } catch (Exception e) {
            e.printStackTrace();
            updateInfoText.setText("下载失败: " + e.getMessage());
            checkUpdateButton.setEnabled(true);
            checkUpdateButton.setText("立即更新");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 存储权限被授予，重新尝试更新
                    performUpdate(pendingVersion);
                } else {
                    updateInfoText.setText("存储权限被拒绝，无法进行更新");
                    checkUpdateButton.setEnabled(true);
                    checkUpdateButton.setText("立即更新");
                }
                break;
                
            default:
                break;
        }
    }
    
}