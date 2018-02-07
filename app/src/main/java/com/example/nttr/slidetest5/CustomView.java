package com.example.nttr.slidetest5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;

/**
 * Created by tamano on 2018/01/26.
 * https://tech.recruit-mp.co.jp/mobile/remember_canvas1/
 */

public class CustomView extends android.support.v7.widget.AppCompatImageView {

    // クラスのpublicな定数
    //https://www.sejuku.net/blog/20977
    public static final int UNDEFINED_RESOURCE = 0;
    private int mBackgroundColor = Color.CYAN;
    //private Paint mPaint = new Paint();

    // View上に、最大4つの画像を貼り付け予定
    private final int BITMAP_COUNT = 4;
    private int[] mResources = new int[BITMAP_COUNT];
    // 整数配列の初期値はnullではないがそのまま扱えない
    // https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q111032016

    private Bitmap[] mBitmaps = new Bitmap[BITMAP_COUNT];

    private final int SELECT_NONE = -1;
    private int mResID = SELECT_NONE;

    // デフォルトコンストラクタ
    // http://www.atmarkit.co.jp/ait/articles/0912/17/news110_2.html
    // 他のコンストラクタを呼ぶ
    public CustomView(Context context) {
        this(context, false);
    }

    // 追加引数付きコンストラクタ
    public CustomView(Context context, boolean isImages) {
        super(context);

        // https://donsyoku.com/zakki/java-initialization-arrays-fill.html
        Arrays.fill(mResources,UNDEFINED_RESOURCE);
        Arrays.fill(mBitmaps,null);

//        // 画像を入れる(=true)か否か(=false)
//        if (isImages) {
//            mResources[1] = R.drawable.arrow_right;
//            mResources[3] = R.drawable.arrow_left;
//
//            // 画像読込（AccBall参照）
//            mBitmaps[1] = BitmapFactory.decodeResource(getResources(),mResources[1]);
//
//            // 画像読込（AccBall参照）
//            mBitmaps[3] = BitmapFactory.decodeResource(getResources(),mResources[3]);
//        }
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        // キャンバス（背景）を透過
//        // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        Bitmap[] bitmapWork = new Bitmap[BITMAP_COUNT];
//
//        int viewWidth = this.getWidth();
//        int viewHeight = this.getHeight();
//
//        int viewWidthHalf = viewWidth / 2;
//        int viewHeightHalf = viewHeight / 2;
//
//        Paint mPaint = new Paint();
//        canvas.drawColor(mBackgroundColor);
//
//        // ビュー内の4箇所（以上）に、配置すべき画像情報を決定して配置する（予定）
////        int i=1;
////        if (mBitmaps[i] != null) {
////            // サイズ補正（AccBall参照）
////            bitmapWork[i] = Bitmap.createScaledBitmap(mBitmaps[i],viewWidthHalf,viewHeightHalf,false);
////            // View上に描画
////            canvas.drawBitmap(bitmapWork[i],viewWidthHalf,0,mPaint);
////        }
////
////        i=3;
////        if (mBitmaps[i] != null) {
////            // サイズ補正（AccBall参照）
////            bitmapWork[i] = Bitmap.createScaledBitmap(mBitmaps[i],
////                    viewWidthHalf,viewHeightHalf,false);
////            // View上に描画
////            canvas.drawBitmap(bitmapWork[i],0,viewHeightHalf,mPaint);
////        }
//
//        //Log.d("onDraw","viewWidthHalf="+viewWidthHalf+",viewHeightHalf="+viewHeightHalf);
//
//        // ビュー内の4箇所に、配置すべき画像情報を加工して配置する
//        for (int j = 0; j < 2 ; j++) {
//            for (int i = 0; i < 2; i++) {
//                int BitmapId = i*2+j;
//                if (mBitmaps[BitmapId] != null) {
//                    // サイズ補正（AccBall参照）
//                    bitmapWork[BitmapId] = Bitmap.createScaledBitmap(mBitmaps[BitmapId],
//                            viewWidthHalf,viewHeightHalf,false);
//                    // View上に描画
//                    canvas.drawBitmap(bitmapWork[BitmapId],
//                            viewWidthHalf * i,viewHeightHalf * j, mPaint);
//                }
//
//            }
//
//        }
//
//    }

    // 画像のリソース情報の取得
    public int[] getRes() {
        return mResources;
    }

    // 画像のリソース情報の設定＋ビットマップ準備
    public void setRes(int[] mResources) {
//        // 配列のコピー
//        // https://developer.android.com/reference/java/util/Arrays.html#copyOf(int[], int)
//        this.mResources = Arrays.copyOf(mResources,BITMAP_COUNT);
//
//        for (int i = 0; i < BITMAP_COUNT; i++) {
//            if (this.mResources[i] != UNDEFINED_RESOURCE) {
//                // 画像読込（AccBall参照）
//                this.mBitmaps[i] = BitmapFactory.decodeResource(getResources(), mResources[i]);
//            } else {
//                this.mBitmaps[i] = null;
//            }
//        }
//
//        // ビュー内の4箇所に、配置すべき画像情報を加工して配置したビットマップを生成
//        int viewWidth = this.getWidth();
//        int viewHeight = this.getHeight();
//        int viewWidthHalf = viewWidth / 2;
//        int viewHeightHalf = viewHeight / 2;
//        // http://cheesememo.blog39.fc2.com/blog-entry-740.html
//        // https://developer.android.com/reference/android/graphics/Bitmap.Config.html
//        Bitmap bitmapWork = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmapWork);;
//
//        for (int j = 0; j < 2 ; j++) {
//            for (int i = 0; i < 2; i++) {
//                int BitmapId = i*2+j;
//                if (mBitmaps[BitmapId] != null) {
//                    // サイズ補正（AccBall参照）
//                    bitmapWork = Bitmap.createScaledBitmap(mBitmaps[BitmapId],
//                            viewWidthHalf,viewHeightHalf,false);
//                    // View上に描画
//                    canvas.drawBitmap(bitmapWork[BitmapId],
//                            viewWidthHalf * i,viewHeightHalf * j, (Paint)null);
//                }
//
//            }
//
//        }

    }

    public int getResID() {
        return mResID;
    }

    public void setResID(int mResID) {
        this.mResID = mResID;
    }
}
