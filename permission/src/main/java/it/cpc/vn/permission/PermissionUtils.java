package it.cpc.vn.permission;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PermissionUtils {
    public interface OnCheckPermission {
        void onGrand(Permission permission);
    }

    private static CompositeDisposable sDisposable = null;

    public static void initPermissionCheck() {
        if (sDisposable == null) {
            sDisposable = new CompositeDisposable();
        }
    }

    public static void disposable() {
        if (sDisposable != null) {
            sDisposable.dispose();
            sDisposable.clear();
        }
    }

    public static String[] mArrayPermissionsStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
            };
        } else {
            return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            };
        }
    }

    public static String[] mArrayPermissionsLocation() {
        return new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] mArrayPermissionsNotification() {
        return new String[]{
            Manifest.permission.POST_NOTIFICATIONS
        };
    }

    public static String[] mArrayPermissionsCamera() {
        return new String[]{Manifest.permission.CAMERA};
    }

    public static String[] mArrayPerMissionRecord() {
        return new String[]{
            Manifest.permission.RECORD_AUDIO
        };
    }

    public static void checkPermissionStore(Fragment fragmentActivity, OnCheckPermission listener) {
        Context context = fragmentActivity.requireContext();
        sDisposable.add(
            new RxPermissions(fragmentActivity)
                .requestEachCombined(mArrayPermissionsStorage())
                .subscribe(permission -> {
                    if (permission.granted) {
                        listener.onGrand(permission);
                    } else {
                        startActivity(context, goToSetting(context), null);
                    }
                    disposable();
                })
        );
    }

    public static void checkPermissionLocation(Fragment fragmentActivity, OnCheckPermission listener) {
        Context context = fragmentActivity.requireContext();
        sDisposable.add(
            new RxPermissions(fragmentActivity)
                .requestEachCombined(mArrayPermissionsLocation())
                .subscribe(permission -> {
                    if (permission.granted) {
                        listener.onGrand(permission);
                    } else {
                        startActivity(context, goToSetting(context), null);
                    }
                    disposable();
                })
        );
    }

    public static void checkPermissionCamera(Fragment fragmentActivity, OnCheckPermission listener) {
        Context context = fragmentActivity.requireContext();
        sDisposable.add(
            new RxPermissions(fragmentActivity)
                .requestEachCombined(mArrayPermissionsCamera())
                .subscribe(permission -> {
                    if (permission.granted) {
                        listener.onGrand(permission);
                    } else {
                        startActivity(context, goToSetting(context), null);
                    }
                    disposable();
                })
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void checkPermissionNotification(Fragment fragmentActivity, OnCheckPermission listener) {
        Context context = fragmentActivity.requireContext();
        sDisposable.add(
            new RxPermissions(fragmentActivity)
                .requestEachCombined(mArrayPermissionsNotification())
                .subscribe(permission -> {
                    if (permission.granted) {
                        listener.onGrand(permission);
                    } else {
//                        startActivity(context, goToSetting(context), null);
                    }
                    disposable();
                })
        );
    }

    public static void checkPermissionRecord(Fragment fragmentActivity, OnCheckPermission listener) {
        sDisposable.add(new RxPermissions(fragmentActivity)
            .requestEachCombined(mArrayPerMissionRecord())
            .subscribe(permission -> {
                if (permission.granted) {
                    listener.onGrand(permission);
                } else {
                }
                disposable();
            })
        );
    }

    private static Intent goToSetting(Context context) {
        String packageName = context.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        return intent;
    }
}
