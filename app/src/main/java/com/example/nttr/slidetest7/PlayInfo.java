package com.example.nttr.slidetest7;

import java.util.ArrayList;
import java.util.Date;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nttr on 2018/02/26.
 * 中断したときのステージ情報を保存し、ステージ再開時に参照し、消去される
 * １操作毎に保存すれば、いつでも再開できそう！？
 */

public class PlayInfo extends RealmObject {
    @PrimaryKey
    public long id;         // 重複登録禁止のためのID
    public Date date;       // 保存日時 or ステージ開始日時

    // 概ねMainActivity上の出現順
    public int pieceX;      // 横方向のパネル数
    public int pieceY;      // 縦方向のパネル数(当面はpieceXと同値とする。異なる場合、各マスが長方形になる)
    public int mode;        // モード

    public int stageNumber = 0;     // ステージ番号。初期値は0。loadNewStage()を呼ぶと1以上になる

    public int moveCount = 0;           // いどう
    public int vanishCount = 0;         // けし
    public int vanishMultiCount = 0;    // 同時けし(非表示)
    public int score = 0;               // すこあ

    // Realmのフィールドに配列、リストは使えない
    // http://grandbig.github.io/blog/2015/06/20/android-realm/
    // デシリアライザ
    // https://qiita.com/Koganes/items/1ab28bf31a49f0cf7dac
    //public int aryImgRes[][];           // 各部位の色と初期位置(但し模様ありのみ)
    public String jsonAryImgRes;

    // 各パネルの模様のID(aryImgResの第1引数)
    //public ArrayList<Integer> imageResources = new ArrayList<>();
    // をJSON化
    public String jsonResIdList;

    // 水色(無い)とピンク(消した)の識別。コード(SELECT_NONE2)追加で対応

}
