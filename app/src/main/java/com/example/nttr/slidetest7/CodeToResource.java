package com.example.nttr.slidetest7;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by nttr on 2018/02/07.
 * コード値をリソースIDへ変換する定数配列っぽいクラス
 */

public class CodeToResource {
    final int SELECT_NONE = -1;
    private ArrayList<Integer> aryResId = new ArrayList<>();
    private int mPatternNumber;

    public CodeToResource() {
        // ピンク、星
        aryResId.add(R.drawable.pink_star_lr); // 0-3 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.pink_star_ll);
        aryResId.add(R.drawable.pink_star_ur);
        aryResId.add(R.drawable.pink_star_ul);
        // 緑、円
        aryResId.add(R.drawable.green_circle_lr); // 4-7 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.green_circle_ll);
        aryResId.add(R.drawable.green_circle_ur);
        aryResId.add(R.drawable.green_circle_ul);
        // 青、四角
        aryResId.add(R.drawable.blue_sguare_lr); // 8-11 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.blue_sguare_ll);
        aryResId.add(R.drawable.blue_sguare_ur);
        aryResId.add(R.drawable.blue_sguare_ul);
        // 黄、三角
        aryResId.add(R.drawable.yellow_triangle_lr); // 12-15 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.yellow_triangle_ll);
        aryResId.add(R.drawable.yellow_triangle_ur);
        aryResId.add(R.drawable.yellow_triangle_ul);
        // 紫、ぶどう
        aryResId.add(R.drawable.purple_grape_lr); // 16-19 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.purple_grape_ll);
        aryResId.add(R.drawable.purple_grape_ur);
        aryResId.add(R.drawable.purple_grape_ul);
        // 赤、さくらんぼ
        aryResId.add(R.drawable.red_cherry_lr); // 20-23 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.red_cherry_ll);
        aryResId.add(R.drawable.red_cherry_ur);
        aryResId.add(R.drawable.red_cherry_ul);
        // オレンジ、みかん
        aryResId.add(R.drawable.orange_mikan_lr); // 24-27 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.orange_mikan_ll);
        aryResId.add(R.drawable.orange_mikan_ur);
        aryResId.add(R.drawable.orange_mikan_ul);
        // 黒、おにぎり
        aryResId.add(R.drawable.black_riceball_lr); // 28-31 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.black_riceball_ll);
        aryResId.add(R.drawable.black_riceball_ur);
        aryResId.add(R.drawable.black_riceball_ul);
        // 黄、バナナ
        aryResId.add(R.drawable.yellow_banana_lr); // 32-35 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.yellow_banana_ll);
        aryResId.add(R.drawable.yellow_banana_ur);
        aryResId.add(R.drawable.yellow_banana_ul);
        // 茶、チョコレート
        aryResId.add(R.drawable.brown_choco_lr); // 36-39 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.brown_choco_ll);
        aryResId.add(R.drawable.brown_choco_ur);
        aryResId.add(R.drawable.brown_choco_ul);
        // 白、歯
        aryResId.add(R.drawable.white_tooth_lr); // 40-43 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.white_tooth_ll);
        aryResId.add(R.drawable.white_tooth_ur);
        aryResId.add(R.drawable.white_tooth_ul);
        // 紺、三日月
        aryResId.add(R.drawable.navy_crescent_lr); // 44-47 右下、左下、右上、左上の順とする（格納位置と対応）
        aryResId.add(R.drawable.navy_crescent_ll);
        aryResId.add(R.drawable.navy_crescent_ur);
        aryResId.add(R.drawable.navy_crescent_ul);

        // 4個セットなので、総数を4で除算。1以上でないとゲームは成立しない
        mPatternNumber = aryResId.size() / 4;
        //Log.d("c2r","mPatternNumber="+mPatternNumber+",aryResId size="+aryResId.size());
    }

    // 指定したリソース番号のリソースIDを返す
    public int getResId(int code) {
        if (0 <= code && code < aryResId.size()){
            return aryResId.get(code);
        }
        return SELECT_NONE;
    }

    // 模様の個数を返す
    public int getPatternNumber() {
        return mPatternNumber;
    }

    // 0から始まる数字をランダムに並べ替え、指定文字数だけを配列で返す。
    // このクラスと関係ない関数だが、このクラスのデータを利用するときに必要
    public int[] getRandomPermutations(int totalNumber, int returnNumber) {
        ArrayList<Integer> population = new ArrayList<>();
        // 並べ替えるリストを作成
        for (int i = 0; i < totalNumber; i++) {
            population.add(i);
        }

        // ランダムに並べ替え
        Collections.shuffle(population);

        // 余計な参考
        //        // リストを配列に変換
        //        // https://qiita.com/kics/items/a1f002a303298061febf
        //        Integer[] res = population.toArray(new Integer[0]);
        //        // 先頭から一部の部分配列
        //        // https://qiita.com/landrunner/items/d4894b3bab649f64a674
        //        res = Arrays.copyOf(res, ret);
        //        return res; // Integer配列なので型が合わない(変換方法が無さそう

        // int配列にするなら、1要素ずつ？
        // http://d.hatena.ne.jp/qsona/20121122/1353595859
        int[] aryResult = new int[returnNumber];
        for (int i = 0; i < returnNumber; i++) {
            aryResult[i] = population.get(i); // Integerをintへそのまま代入OK
        }
        return aryResult;
    }

    // 0から始まるreturnNumber個の数字とSELECT_NONE(-1)をランダムに並べ替えて『リスト』で返す。
    // このクラスと関係ない関数だが、このクラスのデータを利用するときに必要
    // getRandomPermutations()とは全く別物なので注意
    public ArrayList<Integer> getRandomPermutationsPadding(int totalNumber, int returnNumber) {
        ArrayList<Integer> population = new ArrayList<>();
        // 並べ替えるリストを作成
        // 0からではなく、ランダムにreturnNumber個を並べる
        ArrayList<Integer> pattern = new ArrayList<>();
        for (int i = 0; i < mPatternNumber; i++) {
            pattern.add(i);
        }
        Collections.shuffle(pattern);

        for (int i = 0; i < returnNumber; i++) {
            if (i < mPatternNumber) {
                population.add(pattern.get(i));
            } else {
                population.add(SELECT_NONE);
            }
        }
        for (int i = returnNumber; i < totalNumber; i++) {
            population.add(SELECT_NONE);
        }

        // ランダムに並べ替え
        Collections.shuffle(population);

        return population;

//        // int配列にするなら、1要素ずつ？
//        // http://d.hatena.ne.jp/qsona/20121122/1353595859
//        int[] aryResult = new int[totalNumber];
//        for (int i = 0; i < totalNumber; i++) {
//            aryResult[i] = population.get(i); // Integerをintへそのまま代入OK
//        }
//        return aryResult;

    }
}
