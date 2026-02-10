package eg.kesry.loginApp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutMeActivity extends AppCompatActivity {

    private TextView tvCurrentVersion;
    private LinearLayout btnCheckUpdate;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me);

        // 初始化视图组件
        tvCurrentVersion = findViewById(R.id.tv_current_version);
        btnCheckUpdate = findViewById(R.id.btn_check_update);
        btnBack = findViewById(R.id.btn_back);

        // 获取并显示当前版本号
        displayCurrentVersion();

        // 设置检查更新按钮点击事件
        btnCheckUpdate.setOnClickListener(v -> {
            // 跳转到更新检查页面
            Intent intent = new Intent(AboutMeActivity.this, UpdateCheckActivity.class);
            startActivity(intent);
        });

        // 设置返回按钮点击事件
        btnBack.setOnClickListener(v -> {
            finish(); // 关闭当前Activity，返回上一页
        });
    }

    /**
     * 获取并显示当前应用版本号
     */
    private void displayCurrentVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            long versionCode;
            
            // 兼容不同Android版本的versionCode获取方式
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
            
            // 显示版本信息（版本名 + 版本号）
            String versionText = "v" + versionName;
            tvCurrentVersion.setText(versionText);
            
        } catch (Exception e) {
            // 如果获取版本信息失败，显示默认值
            tvCurrentVersion.setText("无法获取版本信息");
            Toast.makeText(this, "无法获取版本信息", Toast.LENGTH_SHORT).show();
        }
    }
}