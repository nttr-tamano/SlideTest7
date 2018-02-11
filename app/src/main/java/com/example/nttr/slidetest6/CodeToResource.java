package com.example.nttr.slidetest6;

import java.util.ArrayList;

/**
 * Created by nttr on 2018/02/07.
 * コード値をリソースIDへ変換する定数配列っぽいクラス
 */

public class CodeToResource {
    final int SELECT_NONE = -1;
    private ArrayList<Integer> aryResID = new ArrayList<>();

    public CodeToResource() {
        // ピンク、星
        aryResID.add(R.drawable.pink_star_lr);      // 0-3 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResID.add(R.drawable.pink_star_ll);
        aryResID.add(R.drawable.pink_star_ur);
        aryResID.add(R.drawable.pink_star_ul);
        // 緑、円
        aryResID.add(R.drawable.green_circle_lr);   // 4-7 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResID.add(R.drawable.green_circle_ll);
        aryResID.add(R.drawable.green_circle_ur);
        aryResID.add(R.drawable.green_circle_ul);
        // 青、四角
        aryResID.add(R.drawable.blue_sguare_lr);    // 8-11 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResID.add(R.drawable.blue_sguare_ll);
        aryResID.add(R.drawable.blue_sguare_ur);
        aryResID.add(R.drawable.blue_sguare_ul);
        // 黄、三角
        aryResID.add(R.drawable.yellow_triangle_lr);  // 12-15 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResID.add(R.drawable.yellow_triangle_ll);
        aryResID.add(R.drawable.yellow_triangle_ur);
        aryResID.add(R.drawable.yellow_triangle_ul);
    }

    public int getResID(int code) {
        if (0 <= code && code < aryResID.size()){
            return aryResID.get(code);
        }
        return SELECT_NONE;
    }

}
