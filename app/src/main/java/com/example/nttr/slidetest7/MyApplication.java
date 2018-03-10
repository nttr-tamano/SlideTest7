package com.example.nttr.slidetest7;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by nttr on 2018/02/26.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Realm初期化
        Realm.init(this);
    }
}
