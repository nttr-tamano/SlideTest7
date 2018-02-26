package com.example.nttr.slidetest7;

import java.util.Date;

import io.realm.annotations.PrimaryKey;

/**
 * Created by nttr on 2018/02/26.
 * 中断したステージ情報を保存し、ステージ再開時に参照し、消去される
 */

public class Suspend {
    @PrimaryKey
    public long id;         // 重複登録禁止のためのID
    public Date date;       // 保存日時

    public int stageNumber; // ステージ番号

    public int moveCount;
    public int vanishCount;
    public int vanishMultiCount;
    public int score;

    public int aryImgRes[][];
    // 各模様の現在位置の更新必要
    // 水色(無い)とピンク(消した)の識別。コード変更が必要？

}
