package com.example.nttr.slidetest6;

import android.app.Activity;
import android.content.Intent;
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

    int intentPieceX = 4;
    int intentPieceY = 4;

    Button btnEasy;
    Button btnEndless;
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
                intentPieceX = Integer.valueOf(itemId) + 3; // 補正。先頭(0) => 3
                intentPieceY = intentPieceX; // 当面は同じ値にする
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        // テキストの設定
        TextView textAppTitle = (TextView) findViewById(R.id.textAppTitle);
        textAppTitle.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_font_name)));

        // ボタンの設定
        btnEasy = (Button) findViewById(R.id.btnEasy);
        btnEasy.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_font_name)));
        btnEndless = (Button) findViewById(R.id.btnEndless);
        btnEndless.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_font_name)));
        btnHard = (Button) findViewById(R.id.btnHard);
        btnHard.setTypeface(Typeface.createFromAsset(getAssets(),getString(R.string.custom_font_name)));

        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEasyClick(view);
            }
        });
    }

    // かんたん 開始
    void btnEasyClick(View v) {
        // 画面の遷移にはIntentというクラスを使用します。
        // Intentは、Android内でActivity同士やアプリ間の通信を行う際の通信内容を記述するクラスです。
        Intent intent = new Intent(this, MainActivity.class);
        // Intentに渡す引数
        intent.putExtra("PieceX",intentPieceX);
        intent.putExtra("PieceY",intentPieceY);

        // startActivityで、Intentの内容を発行します。
        startActivity(intent);

    }
}
