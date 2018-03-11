package com.example.nttr.slidetest7;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;

public class TitleActivity extends AppCompatActivity {

    // デバッグモード 管理用
    final boolean flagDebug = true;    // リリース時はfalseにすること

    // Intent
    // 定数(引数決定用)
    static final int REQUEST_TRIAL = 1;
    static final int REQUEST_EASY = 2;
    static final int REQUEST_SURVIVAL = 3;
    static final int REQUEST_HARD = 4;
    static final int REQUEST_RESUME = 5;
    // 定数(Mode用)
    static final int INTENT_MODE_TRIAL    = 0;
    static final int INTENT_MODE_EASY     = 1;
    static final int INTENT_MODE_SURVIVAL = 2;
    static final int INTENT_MODE_HARD     = 3;

    int intentPieceX = 4;
    int intentPieceY = 4;

    // 中断の管理
    static final int RETURN_YES = 1;
    static final int RETURN_NO  = 2;
    boolean flagExistSuspend = false;

    // Realm
    Realm mRealm;

    // 画面要素
    Button btnTrial;
    Button btnEasy;
    Button btnSurvival;
    Button btnHard;
    Button btnRealmTest;    // Realm確認用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // プルダウンの設定
        Spinner spinner = findViewById(R.id.spinner);

        // https://akira-watson.com/android/spinner.html
        String spinnerItems[] = {"3x3 マス", "4x4 マス", "5x5 マス", "6x6 マス"};
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        spinner.setAdapter(adapter);
        // デフォルト変更
        // http://onlineconsultant.jp/pukiwiki/?Android%20Spinner%E3%81%AB%E3%83%87%E3%83%95%E3%82%A9%E3%83%AB%E3%83%88%E3%82%92%E8%A8%AD%E5%AE%9A
        spinner.setSelection(1); // 2番目をデフォルト表示
        // フォント変更
        // テキストサイズ変更
        // http://skys.co.jp/archives/4714
        // 180213: 文字サイズ変更に少し手間がかかるので後回し

        // ↓これは無理
        //spinner.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_font_name)));

        // リスナーを登録
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                //String item = (String)spinner.getSelectedItem();
                //textView.setText(item);
                //Log.d("spinner",item+" is selected.");

                int itemId = (int)spinner.getSelectedItemId();
                intentPieceX = itemId + 3; // 補正。先頭(0) => 3
                intentPieceY = intentPieceX; // 当面は同じ値にする
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        // フォント指定に必要な値の取得
        AssetManager asset = getAssets();
        String fontName = getString(R.string.custom_font_name);

        // テキストの設定
        TextView textAppTitle = (TextView) findViewById(R.id.textAppTitle);
        textAppTitle.setTypeface(Typeface.createFromAsset(asset, fontName));

        // ボタンの設定
        btnTrial = (Button) findViewById(R.id.btnTrial);
        btnTrial.setTypeface(Typeface.createFromAsset(asset, fontName));
        btnEasy = (Button) findViewById(R.id.btnEasy);
        btnEasy.setTypeface(Typeface.createFromAsset(asset, fontName));
        btnSurvival = (Button) findViewById(R.id.btnSurvival);
        btnSurvival.setTypeface(Typeface.createFromAsset(asset, fontName));
        btnHard = (Button) findViewById(R.id.btnHard);
        btnHard.setTypeface(Typeface.createFromAsset(asset, fontName));

        // Realm開始
        mRealm = Realm.getDefaultInstance();

        // 中断データの有無をチェック
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // 全レコード取得、リスト化
                RealmResults<PlayInfo> playInfos
                        = realm.where(PlayInfo.class).findAll();

                if (playInfos.size() > 0) {
                    flagExistSuspend = true;
                }
            }
        });

        // ボタンクリック時の動作の定義
        btnTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view, REQUEST_TRIAL);
            }
        });
        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view, REQUEST_EASY);
            }
        });
        btnSurvival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view, REQUEST_SURVIVAL);
            }
        });
        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view, REQUEST_HARD);
            }
        });

        // Realmテスト用
        btnRealmTest = (Button) findViewById(R.id.btnRealmTest);
        if (flagDebug) {
            btnRealmTest.setVisibility(View.VISIBLE);
        }
        btnRealmTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TitleActivity.this,RealmTestActivity.class);
                startActivity(intent);
            }
        });

    }

    // ボタンクリックで、各モード開始
    void buttonClick(View v, int requestCode) {
        // 画面の遷移にはIntentというクラスを使用します。
        // Intentは、Android内でActivity同士やアプリ間の通信を行う際の通信内容を記述するクラスです。
        Intent intent = new Intent(this, MainActivity.class);
        final String INTENT_NAME_PIECE_X = getString(R.string.intent_name_piece_x);
        final String INTENT_NAME_PIECE_Y = getString(R.string.intent_name_piece_y);
        final String INTENT_NAME_MODE = getString(R.string.intent_name_mode);
        // Intentに渡す引数
        switch (requestCode) {
            case REQUEST_TRIAL:
                // とらいある
                intent.putExtra(INTENT_NAME_PIECE_X, 4);   // 固定
                intent.putExtra(INTENT_NAME_PIECE_Y, 4);   // 固定
                intent.putExtra(INTENT_NAME_MODE, INTENT_MODE_TRIAL);
                break;
            case REQUEST_EASY:
                // かんたん
                intent.putExtra(INTENT_NAME_PIECE_X, intentPieceX);
                intent.putExtra(INTENT_NAME_PIECE_Y, intentPieceY);
                intent.putExtra(INTENT_NAME_MODE, INTENT_MODE_EASY);
                break;
            case REQUEST_SURVIVAL:
                // いつまでも
                intent.putExtra(INTENT_NAME_PIECE_X, intentPieceX);
                intent.putExtra(INTENT_NAME_PIECE_Y, intentPieceY);
                intent.putExtra(INTENT_NAME_MODE, INTENT_MODE_SURVIVAL);
                break;
            case REQUEST_HARD:
                // むずかしい
                intent.putExtra(INTENT_NAME_PIECE_X, intentPieceX);
                intent.putExtra(INTENT_NAME_PIECE_Y, intentPieceY);
                intent.putExtra(INTENT_NAME_MODE, INTENT_MODE_HARD);
                break;
        }

        // startActivityで、Intentの内容を発行します。
        startActivityForResult(intent, requestCode);

    }

    // アクティビティ終了
    @Override
    protected void onDestroy() {
        // Realm終了
        mRealm.close();
        super.onDestroy();
    }

    // ダイアログからのコールバック
    // http://www.ipentec.com/document/android-custom-dialog-using-dialogfragment-return-value
    public void onReturnValue(int value) {
        // 「はい」のとき
        if (value == RETURN_YES) {
            // 中断ステージ再開
            buttonClick((View)null, REQUEST_RESUME);
        }
    }
    // タイトルに戻った時の処理
    // https://qiita.com/kskso9/items/01c8bbb39355af9ec25e
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result",requestCode+" was returned.");

        //TODO 返ってきたときの処理を記載

    }
}
