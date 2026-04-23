package com.manager.assistant.helpers;

import android.util.Log;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.manager.assistant.generic_enums.LogTags;

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
    public static void showBiometricPrompt(@NonNull FragmentActivity activity, AuthCallback callback) {
        //获取主线程执行器
        Executor executor = ContextCompat.getMainExecutor(activity);

        //定义验证结果回调
        BiometricPrompt biometricPrompt = getBiometricPrompt(activity, callback, executor);

        BiometricManager biometricManager = BiometricManager.from(activity);
        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        //配置对话框信息
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("您已开启身份验证")
                .setSubtitle("请使用指纹或面部识别")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        //显示身份验证对话框
        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            if (result == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
                Log.e(LogTags.BIOMETRIC_HELPER.getV(), "设备不支持生物识别");
            } else if (result == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
                Log.e(LogTags.BIOMETRIC_HELPER.getV(), "硬件忙或不可用");
            } else if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                Log.e(LogTags.BIOMETRIC_HELPER.getV(), "用户未设置指纹或锁屏密码");
            }
        }
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
