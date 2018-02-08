package com.example.nttr.slidetest5;

import java.util.ArrayList;

/**
 * Created by nttr on 2018/02/07.
 * コード値をリソースIDへ変換する定数配列っぽいクラス
 */

public class CodeToResource {
    final int SELECT_NONE = -1;
    private ArrayList<Integer> aryResID = new ArrayList<>();

    public CodeToResource() {
        aryResID.add(R.drawable.pink_star_lr);  // 0-3 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResID.add(R.drawable.pink_star_ll);
        aryResID.add(R.drawable.pink_star_ur);
        aryResID.add(R.drawable.pink_star_ul);
    }

    public int getResID(int code) {
        if (0 <= code && code < aryResID.size()){
            return aryResID.get(code);
        }
        return SELECT_NONE;
    }

}
