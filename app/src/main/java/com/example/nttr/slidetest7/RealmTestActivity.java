package com.example.nttr.slidetest7;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/*
 * ScheduleBookのものを流用し、クラスとメンバを別クラス用に書き換えた
 */

public class RealmTestActivity extends AppCompatActivity {

    Realm mRealm;

    TextView textView;
    Button create;
    Button read;
    Button update;
    Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_test);

        // Realm インスタンス取得
        mRealm = Realm.getDefaultInstance();

        textView = (TextView) findViewById(R.id.textView);;
        create = (Button) findViewById(R.id.create);
        read = (Button) findViewById(R.id.read);
        update = (Button) findViewById(R.id.update);
        delete = (Button) findViewById(R.id.delete);

        Button back= (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 登録
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
//                        // 自動採番。最大値を取得し、その次(+1)をidとする
//                        Number max = realm.where(PlayInfo.class).max("id");
//                        long newId = 0;
//                        if (max != null) {
//                            newId = max.longValue() + 1;
//                        }
//
//                        PlayInfo playInfo = realm.createObject(PlayInfo.class,newId);
//                        playInfo.date = new Date();
//                        playInfo.title = "登録テスト";
//                        playInfo.detail = "スケジュールの詳細情報です";
//
//                        // 保存するスケジュールをtextViewへ表示
//                        textView.setText("登録しました\n"
//                                + playInfo.toString());
                    }
                });
            }
        });

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<PlayInfo> playInfos
                                = realm.where(PlayInfo.class).findAll();

                        // 取得した全スケジュールをtextViewへ表示
                        textView.setText("取得");
                        for (PlayInfo playInfo:
                             playInfos) {
                            String text = textView.getText() + "\n"
                                    + playInfo.toString();
                            textView.setText(text);
                        }
                    }
                });
            }
        });

        // 更新
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
//                        PlayInfo playInfo = realm.where(PlayInfo.class)
//                                .equalTo("id",0)
//                                .findFirst();
                        // idが最小のレコードを取得できれば更新
                        Number min = realm.where(PlayInfo.class).min("id");
                        if (min != null) {
                            PlayInfo playInfo = realm.where(PlayInfo.class)
                                    .equalTo("id", min.longValue())
                                    .findFirst();

                            // 書き換えテスト
                            playInfo.moveCount += 10;
                            playInfo.date = new Date();

                            // 更新したスケジュールをtextViewへ表示
                            textView.setText("更新しました\n"
                                    + playInfo.toString());
                        }
                    }
                });
            }
        });

        // 削除
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // idが最小のレコードを取得できれば削除
                        Number min = realm.where(PlayInfo.class).min("id");
                        if (min != null) {
                            PlayInfo playInfo = realm.where(PlayInfo.class)
                                    .equalTo("id",min.longValue())
                                    .findFirst();

                            playInfo.deleteFromRealm();

                            // 削除したことをtextViewへ表示
                            textView.setText("削除しました\n"
                                    + playInfo.toString());
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Real クローズ
        mRealm.close();

        super.onDestroy();
    }
}
