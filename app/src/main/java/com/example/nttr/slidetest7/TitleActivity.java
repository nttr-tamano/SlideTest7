package com.example.nttr.slidetest7;

import android.app.DialogFragment;
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
    static final int REQUEST_NONE     = -1;
    static final int REQUEST_TRIAL    =  1;
    static final int REQUEST_EASY     =  2;
    static final int REQUEST_SURVIVAL =  3;
    static final int REQUEST_HARD     =  4;
    static final int REQUEST_RESUME   =  5;
    // 定数(Mode用) 上記と1:1対応していないので別定数とした
    static final int INTENT_MODE_TRIAL    = 0;
    static final int INTENT_MODE_EASY     = 1;
    static final int INTENT_MODE_SURVIVAL = 2;
    static final int INTENT_MODE_HARD     = 3;

    // 中断データを再開しないときの requestCode の値
    int noResumeCode = REQUEST_NONE;

    int selectPieceX = 4;
    int selectPieceY = 4;

    // 中断の管理、ダイアログから参照
    static final int RETURN_RESUME = 1;
    static final int RETURN_NEW = 2;
    static final int RETURN_NO  = 3;
    boolean flagExistSuspend = false;
    PlayInfo mPlayInfoResume;

    // Realm
    Realm mRealm;

    // 画面要素
    Button btnTrial;
    Button btnEasy;
    Button btnSurvival;
    Button btnHard;
    Button btnRealmTest;            // Realm確認用
    boolean flagCalledOnce = false; // 初回呼び出し済=true、未=false

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
                selectPieceX = itemId + 3; // 補正。先頭(0) => 3
                selectPieceY = selectPieceX; // 当面は同じ値にする
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

        // ボタンクリック時の動作の定義
        btnTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectResumeOrStart(view, REQUEST_TRIAL);
            }
        });
        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectResumeOrStart(view, REQUEST_EASY);
            }
        });
        btnSurvival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectResumeOrStart(view, REQUEST_SURVIVAL);
            }
        });
        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectResumeOrStart(view, REQUEST_HARD);
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

    // onCreate直後の初回処理用
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // 初回呼び出しでなければ処理中断
        if (flagCalledOnce) {
            return;
        }

        // 呼び出したのでフラグ更新
        flagCalledOnce = true;

        // 中断データのチェック及び再開
        selectResumeOrStart((View)null, REQUEST_NONE);
    }

    // 中断データがあれば再開確認、なければ新規スタート
    void selectResumeOrStart(View view, int code) {
        if (checkSuspend()) {
            noResumeCode = code;
            // 中断データを再開するか確認ダイアログを開く → onReturnValue()
            DialogFragment newFragment = new ResumeDialog();
            newFragment.show(getFragmentManager(), "resume");
        } else {
            buttonClick(view, code);
        }
    }

    // 中断データの有無をチェックし、あればtrueを返す
    // 要: Realm開始済
    // TODO: executeTransactionで記述しているためフラグ flagExistSuspend を
    // グローバル変数にしているのが整理不足か
    boolean checkSuspend() {
        flagExistSuspend = false;
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // 全レコード取得、リスト化
                // PKなidをキーとしているため、1個しかないはず
                RealmResults<PlayInfo> playInfos
                        = realm.where(PlayInfo.class).equalTo("id",0).findAll();

                // 結果が空でなければ、最初のレコードを保持
                if (playInfos.size() > 0) {
                    flagExistSuspend = true;
                    mPlayInfoResume = playInfos.get(0);
                }
            }
        });
        return flagExistSuspend;
    }

    // ボタンクリックで、各モード開始
    void buttonClick(View v, int requestCode) {
        // 画面の遷移にはIntentというクラスを使用します。
        // Intentは、Android内でActivity同士やアプリ間の通信を行う際の通信内容を記述するクラスです。
        Intent intent = new Intent(this, MainActivity.class);

        // Intentに渡す引数の名称
        final String INTENT_NAME_PIECE_X = getString(R.string.intent_name_piece_x);
        final String INTENT_NAME_PIECE_Y = getString(R.string.intent_name_piece_y);
        final String INTENT_NAME_MODE = getString(R.string.intent_name_mode);
        final String INTENT_NAME_RESUME = "Resume";

        // Intentに渡す引数の値
        int intentPieceX;
        int intentPieceY;
        int intentMode;
        boolean intentResume;

        switch (requestCode) {
            case REQUEST_TRIAL:
                // とらいある
                intentPieceX = 4;
                intentPieceY = 4;
                intentMode = INTENT_MODE_TRIAL;
                intentResume = false;
                break;
            case REQUEST_EASY:
                // かんたん
                intentPieceX = selectPieceX;
                intentPieceY = selectPieceY;
                intentMode = INTENT_MODE_EASY;
                intentResume = false;
                break;
            case REQUEST_SURVIVAL:
                // さばいばる
                intentPieceX = selectPieceX;
                intentPieceY = selectPieceY;
                intentMode = INTENT_MODE_SURVIVAL;
                intentResume = false;
                break;
            case REQUEST_HARD:
                // むずかしい
                intentPieceX = selectPieceX;
                intentPieceY = selectPieceY;
                intentMode = INTENT_MODE_HARD;
                intentResume = false;
                break;
            case REQUEST_RESUME:
                // 再開。上記の他のいずれかのモードになっている
                intentPieceX = mPlayInfoResume.pieceX;
                intentPieceY = mPlayInfoResume.pieceY;
                intentMode = mPlayInfoResume.mode;
                intentResume = true;    // ここだけ true
                break;
            default:
                // 上記以外のモードでは処理中断
                return;
        }

        // 決定した引数をIntentにセット
        intent.putExtra(INTENT_NAME_PIECE_X, intentPieceX);
        intent.putExtra(INTENT_NAME_PIECE_Y, intentPieceY);
        intent.putExtra(INTENT_NAME_MODE, intentMode);
        intent.putExtra(INTENT_NAME_RESUME, intentResume);

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
        if (value == RETURN_RESUME) {
            // 中断ステージ再開
            buttonClick((View)null, REQUEST_RESUME);
        } else if (value == RETURN_NEW) {
            // 新規再開
            if (noResumeCode != REQUEST_NONE) {
                buttonClick((View)null, noResumeCode);
            }
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
