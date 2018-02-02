package com.example.nttr.slidetest5;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by nttr on 2018/01/31.
 * http://ichitcltk.hustle.ne.jp/gudon2/index.php?pageType=file&id=Android035_Graphics2_SurfaceView
 */

public class TranslationSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private int mSrcX = 0;
    private int mSrcY = 0;
    private int mDestX = 0;
    private int mDestY = 0;
    private int mDeltaX = 0;
    private int mDeltaY = 0;

    private ValueAnimator mAnimator;
    private SurfaceHolder mHolder;

    private Bitmap mBitmap;

    private boolean isAttached = true;  //　不要になるのが理想

    // コンストラクタ
    // activity_main.xml に配置利用を想定
    // http://ojed.hatenablog.com/entry/2015/12/05/161013
    public TranslationSurfaceView(Context context, AttributeSet attr) {
        super(context, attr);

        mHolder = getHolder();
        // コールバック設定
        mHolder.addCallback(this);
        // 透過
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        // 一番手前に表示
        setZOrderOnTop(true);


        // ValueAnimatorの初期設定
        mAnimator = ValueAnimator.ofFloat(0.F, 1.F);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
                //Log.d("onAnimateUpdate","invalidate()");
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        // 描画する座標(矩形の左上)の決定
        // mAnimator.getAnimatedValue() == 0..1
        float animeVal = (float)mAnimator.getAnimatedValue();

        int drawX = (int)(mSrcX + animeVal * mDeltaX);
        int drawY = (int)(mSrcY + animeVal * mDeltaY);

        // 行き過ぎの補正（しなくていい？）

        // キャンバス（背景）を透過
        // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 画像を描画
        Paint p = new Paint();
        canvas.drawBitmap(mBitmap, drawX, drawY, p);
        // 描画終了
        //mHolder.unlockCanvasAndPost(canvas);

        //Log.d("onDraw","animeVal="+animeVal);
    }

    // SurfaceView生成時は、透過させる
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // https://qiita.com/circularuins/items/a61c5e7149f355a54a8b
        Canvas canvas = holder.lockCanvas();

        // キャンバス（背景）を透過
        // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // 描画終了
        mHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // SurfaceView終了時
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isAttached = false;
        mAnimator = null;
        // mThread = null; //スレッドを終了
    }

    // ValueAnimatorを返す。.start()を呼べばメール開始
    // 次のsetとまとめて行った方が良い？
    public ValueAnimator getmAnimator() {
        return mAnimator;
    }

    // アニメーション開始に必要な情報の設定
    public void setAnimationInfo(Bitmap mBitmap, int mSrcX, int mSrcY, int mDestX, int mDestY) {
        this.mBitmap = mBitmap;
        this.mSrcX = mSrcX;
        this.mSrcY = mSrcY;
        this.mDestX = mDestX;
        this.mDestY = mDestY;

        // 移動距離も計算
        this.mDeltaX = mDestX - mSrcX;
        this.mDeltaY = mDestY - mSrcY;
        Log.d("setAnimationInfo","mSrcX="+mSrcX+",mSrcY="+mSrcY+",mDeltaX="+mDeltaX+",mDeltaY="+mDeltaY);

    }


}
