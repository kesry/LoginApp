# Android 14 APK下载权限适配说明

## 问题描述
在Android 14设备上点击下载APK更新包时出现文件权限问题，主要原因是：
1. 传统存储权限在新版本Android中受限
2. 权限检查逻辑未适配Android 13+的新权限体系
3. 缺少通知权限导致无法显示下载进度

## 解决方案

### 1. AndroidManifest.xml 修改

#### 精简权限声明（专为APK下载优化）：
```xml
<!-- Android 13+通知权限（必需） -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- 传统存储权限（仅限旧版本Android） -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="29" />
```

#### 移除的权限：
- ❌ `READ_MEDIA_IMAGES` - 不适用于APK下载场景

### 2. UpdateCheckActivity.java 修改

#### 优化的权限检查逻辑：
```java
private boolean checkStoragePermissions() {
    List<String> permissionsNeeded = new ArrayList<>();
    
    // Android 13+ 使用DownloadManager不需要特殊存储权限
    // 但需要通知权限来显示下载进度
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }
    } else {
        // Android 6.0-12 需要传统的存储权限
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
            permissionsNeeded.toArray(new String[0]), 1);
        return false;
    }
    return true;
}
```

## 权限适配策略（APK下载专用）

### Android 版本对应权限需求：

| Android版本 | 所需权限 | 说明 |
|------------|---------|------|
| Android 13+ | POST_NOTIFICATIONS | 仅需通知权限，DownloadManager自动处理存储 |
| Android 10-12 | READ_EXTERNAL_STORAGE | 分区存储模式下的读取权限 |
| Android 9 及以下 | READ_EXTERNAL_STORAGE + WRITE_EXTERNAL_STORAGE | 传统存储权限 |

### 关键改进点：

1. **场景化权限设计**：针对APK下载场景优化，避免不必要的权限
2. **最小权限原则**：Android 13+只需通知权限即可完成下载
3. **DownloadManager优势**：利用系统下载管理器的内置权限处理
4. **版本智能判断**：自动识别Android版本并申请相应权限

## 技术原理

### 为什么Android 13+不需要存储权限？

从Android 13开始，使用`DownloadManager`下载文件到公共目录（如Downloads）时：
- 系统会自动处理存储权限
- 应用无需申请`WRITE_EXTERNAL_STORAGE`
- 只需要`POST_NOTIFICATIONS`来显示下载通知

### DownloadManager的优势：
- 系统级下载管理
- 自动处理权限和存储位置
- 提供下载进度通知
- 支持断点续传
- 后台下载能力

## 测试验证

构建命令：
```bash
./gradlew assembleDebug
```

验证要点：
- ✅ Android 14设备上仅请求通知权限
- ✅ 下载功能正常使用
- ✅ 通知能够正常显示下载进度
- ✅ 向下兼容旧版本Android设备
- ✅ 不会请求不必要的媒体权限

## 注意事项

1. **权限申请时机**：建议在首次触发下载时申请权限
2. **用户体验**：权限说明文案要清晰告知用户用途
3. **版本兼容**：代码已处理各版本差异，无需额外适配
4. **安全合规**：符合Google Play权限使用政策

## 性能优势

相比之前的方案，新方案具有以下优势：
- 减少了2个不必要的权限声明
- 降低了权限申请的复杂度
- 提升了应用的安全性和用户信任度
- 简化了权限管理逻辑