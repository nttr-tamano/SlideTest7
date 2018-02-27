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

public class TitleActivity extends AppCompatActivity {

    // Intent
    final int REQUEST_TRIAL = 1;
    final int REQUEST_EASY = 2;
    final int REQUEST_SURVIVAL = 3;
    final int REQUEST_HARD = 4;

    final int INTENT_MODE_TRIAL   = 0;
    final int INTENT_MODE_EASY    = 1;
    final int INTENT_MODE_SURVIVAL = 2;
    final int INTENT_MODE_HARD    = 3;

    int intentPieceX = 4;
    int intentPieceY = 4;

    Button btnTrial;
    Button btnEasy;
    Button btnSurvival;
    Button btnHard;

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
    }

    // ボタンクリックで、各モード開始
    void buttonClick(View v, int requestCode) {
        // 画面の遷移にはIntentというクラスを使用します。
        // Intentは、Android内でActivity同士やアプリ間の通信を行う際の通信内容を記述するクラスです。
        Intent intent = new Intent(this, MainActivity.class);
        String intentNamePieceX = getString(R.string.intent_name_piece_x);
        String intentNamePieceY = getString(R.string.intent_name_piece_y);
        String intentNameMode = getString(R.string.intent_name_mode);
        // Intentに渡す引数
        switch (requestCode) {
            case REQUEST_TRIAL:
                // とらいある
                intent.putExtra(intentNamePieceX, 4);   // 固定
                intent.putExtra(intentNamePieceY, 4);   // 固定
                intent.putExtra(intentNameMode, INTENT_MODE_TRIAL);
                break;
            case REQUEST_EASY:
                // かんたん
                intent.putExtra(intentNamePieceX, intentPieceX);
                intent.putExtra(intentNamePieceY, intentPieceY);
                intent.putExtra(intentNameMode, INTENT_MODE_EASY);
                break;
            case REQUEST_SURVIVAL:
                // いつまでも
                intent.putExtra(intentNamePieceX, intentPieceX);
                intent.putExtra(intentNamePieceY, intentPieceY);
                intent.putExtra(intentNameMode, INTENT_MODE_SURVIVAL);
                break;
            case REQUEST_HARD:
                // むずかしい
                intent.putExtra(intentNamePieceX, intentPieceX);
                intent.putExtra(intentNamePieceY, intentPieceY);
                intent.putExtra(intentNameMode, INTENT_MODE_HARD);
                break;
        }

        // startActivityで、Intentの内容を発行します。
        startActivityForResult(intent, requestCode);

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
