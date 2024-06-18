package it.cpc.vn.permission;

import androidx.activity.result.ActivityResultLauncher;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class RxPermissionViewModel extends ViewModel {
    // Contains all the current permission requests.
    // Once granted or denied, they are removed from it.
    final Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();
    public boolean mLogging;
    ActivityResultLauncher<String[]> permissionRequest = null;
}