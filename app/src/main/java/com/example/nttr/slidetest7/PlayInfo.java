package com.example.nttr.slidetest7;

import java.util.Date;

import io.realm.annotations.PrimaryKey;

/**
 * Created by nttr on 2018/02/26.
 * 中断したときのステージ情報を保存し、ステージ再開時に参照し、消去される
 * １操作毎に保存すれば、いつでも再開できそう！？
 */

public class PlayInfo {
    @PrimaryKey
    public long id;         // 重複登録禁止のためのID
    public Date date;       // 保存日時 or ステージ開始日時

    // 概ねMainActivity上の出現順
    public int pieceX;      // 横方向のパネル数
    public int pieceY;      // 縦方向のパネル数(当面はpieceXと同じ値)
    public int mode;        // モード

    public int stageNumber; // ステージ番号

    public int moveCount;
    public int vanishCount;
    public int vanishMultiCount;
    public int score;

    public int aryImgRes[][];
    // 各模様の現在位置の更新必要
    // 水色(無い)とピンク(消した)の識別。コード変更が必要？

    // 最新のパネルの位置はどこで持つ？

}
