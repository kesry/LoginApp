# Android项目混淆配置指南

## 概述

本项目已配置完整的代码混淆功能，用于保护应用程序的源代码和资源，提高应用的安全性。

## 配置详情

### 1. 构建配置 (app/build.gradle)

```gradle
buildTypes {
    release {
        minifyEnabled true          // 启用代码混淆
        shrinkResources true        // 启用资源压缩
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
    debug {
        minifyEnabled false         // Debug版本不启用混淆，便于调试
        proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
    }
}
```

### 2. 混淆规则 (app/proguard-rules.pro)

混淆规则文件包含了以下保护策略：

#### 基础保护
- 保留Android四大组件不被混淆
- 保留自定义View类的构造方法
- 保留枚举类和Parcelable实现类
- 保留注解和反射相关信息

#### 第三方库保护
- **Jackson JSON**: 保护JSON序列化/反序列化相关类
- **OkHttp3**: 保护网络请求相关类
- **ZXing**: 保护二维码扫描相关类
- **AndroidX**: 保护AndroidX组件
- **Material Design**: 保护Google Material组件

#### 业务代码保护
- 保留项目主包 `eg.kesry.loginApp` 下的所有类
- 保留回调接口和监听器
- 保留数据模型(Bean)类
- 保留工具类和适配器类

## 构建命令

### 构建Release版本（带混淆）
```bash
./gradlew assembleRelease
```

### 构建Debug版本（不带混淆）
```bash
./gradlew assembleDebug
```

## 输出文件位置

构建完成后，APK文件位于：
```
app/build/outputs/apk/release/app-release.apk
app/build/outputs/apk/debug/app-debug.apk
```

## 混淆映射文件

混淆后的映射文件位于：
```
app/build/outputs/mapping/release/mapping.txt
```

这个文件记录了类名、方法名的混淆前后对照关系，用于线上问题调试。

## 常见问题处理

### 1. 运行时ClassNotFoundException
如果出现类找不到的错误，请在proguard-rules.pro中添加相应的keep规则：

```proguard
-keep class 完整类名 { *; }
```

### 2. 方法调用失败
如果是通过反射调用的方法出现问题，需要保留相应的方法：

```proguard
-keepclassmembers class 类名 {
    方法签名;
}
```

### 3. JNI方法丢失
Native方法需要特别保护：

```proguard
-keepclasseswithmembernames class * {
    native <methods>;
}
```

## 最佳实践

1. **定期测试**: 每次添加新功能后都要测试混淆版本
2. **保留必要信息**: 不要过度混淆，确保功能性不受影响
3. **保存映射文件**: 妥善保管mapping.txt文件用于问题追踪
4. **渐进式混淆**: 可以先对部分代码进行混淆测试
5. **监控Crash**: 上线后密切关注混淆版本的崩溃日志

## 安全建议

- 敏感信息不要硬编码在代码中
- 使用NDK处理核心算法逻辑
- 定期更新混淆规则
- 结合其他安全措施（如签名校验、代码加固等）

## 参考资料

- [Android官方ProGuard文档](https://developer.android.com/studio/build/shrink-code)
- [ProGuard官方手册](https://www.guardsquare.com/manual/home)

