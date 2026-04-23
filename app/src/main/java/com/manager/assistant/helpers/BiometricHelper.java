package com.manager.assistant.helpers;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricHelper {
    public interface AuthCallback {
        /**
         * 验证成功回调
         */
        void onSuccess();

        /**
         * 验证失败回调
         */
        void onError();
    }

    /**
     * 进行身份验证
     *
     * @param activity 需要弹出身份验证对话框的活动界面
     * @param callback 身份验证回调
     */
    public static void showBiometricPrompt(FragmentActivity activity, AuthCallback callback) {
        // 1. 获取主线程执行器
        Executor executor = ContextCompat.getMainExecutor(activity);

        // 2. 定义验证结果回调
        BiometricPrompt biometricPrompt = getBiometricPrompt(activity, callback, executor);

        // 4. 配置对话框信息
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("您已开启身份验证")
                .setSubtitle("请使用指纹或面部识别")
                .setNegativeButtonText("使用锁屏密码") // 点击此按钮将切换到系统锁屏验证
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        // 5. 显示验证界面
        biometricPrompt.authenticate(promptInfo);
    }

    @NonNull
    private static BiometricPrompt getBiometricPrompt(FragmentActivity activity, AuthCallback callback, Executor executor) {
        BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onError();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // 此时指纹/面部未识别成功，可以给用户提示
            }
        };

        // 3. 构建 BiometricPrompt
        return new BiometricPrompt(activity, executor, authCallback);
    }
}
