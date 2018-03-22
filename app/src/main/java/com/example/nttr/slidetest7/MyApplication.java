package com.example.nttr.slidetest7;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by nttr on 2018/02/26.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Realm初期化
        Realm.init(this);

        // https://stackoverflow.com/questions/33940233/realm-migration-needed-exception-in-android-while-retrieving-values-from-real
        // Realm　スキーマ変更時等に必須
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        // スキーマ変更等でDBをクリアしたいときは、以下を有効化
        // https://qiita.com/matsuyoro/items/7e19ac6e87090e2c87b3
        // Configure Realm for the application
//        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
//        Realm.deleteRealm(realmConfiguration); // Clean slate
//        Realm.setDefaultConfiguration(realmConfiguration); // Make this Realm the default
    }
}
