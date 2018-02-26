package com.example.nttr.slidetest7;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by nttr on 2018/01/31.
 * http://ichitcltk.hustle.ne.jp/gudon2/index.php?pageType=file&id=Android035_Graphics2_SurfaceView
 * ValueAnimator、コールバック、アニメーション情報を追加したカスタムSurfaceView。activity_main.xmlにも使用
 */

public class TranslationSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    // アニメーションの座標情報
    private int mSrcX = 0;
    private int mSrcY = 0;
    private int mDeltaX = 0;
    private int mDeltaY = 0;

    // アニメーションで描画する画像
    private Bitmap mBitmap;

    // 主なメンバ
    private ValueAnimator mAnimator;
    private SurfaceHolder mHolder;

    // コンストラクタ
    // activity_main.xml に配置利用を想定
    // http://ojed.hatenablog.com/entry/2015/12/05/161013
    // https://qiita.com/androhi/items/f5870a27b6e0a4fd5c3d
    public TranslationSurfaceView(Context context) {
        super(context);
        initialize();
    }

    public TranslationSurfaceView(Context context, AttributeSet attr) {
        super(context, attr);
        initialize();
    }

    // コンストラクタの共通部分を別関数化
    private void initialize() {
        mHolder = getHolder();

        // 透過
        //mHolder.setFormat(PixelFormat.TRANSPARENT);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        // コールバック設定
        mHolder.addCallback(this);

        //// フォーカス可
        //setFocusable(true);

        // 背景を透過させる
        // https://groups.google.com/forum/#!topic/android-group-japan/fQ10g8EgxNk
        //setAlpha(1.0F);
        setAlpha(0.99F); //180204: 要確認。透過を1にすると、背景が真っ黒になる
        setBackgroundColor(0);
        //setBackground(null);

        // 一番手前に表示
        setZOrderOnTop(true);

        // ValueAnimatorの初期設定
        mAnimator = ValueAnimator.ofFloat(0.F, 1.F);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate(); // onDraw()が呼ばれる
                // canvas管理の必要な独自関数と他のタイミングでも呼ばれるonDrawとどっちが良いか不明
                //Log.d("onAnimateUpdate","invalidate()");
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        // アニメーション中でなければ実行しない
        if ((mAnimator == null) || (!mAnimator.isRunning())) {
            return;
        }

        // 描画する座標(矩形の左上)の決定
        // mAnimator.getAnimatedValue() == 0..1
        float animeVal = (float)mAnimator.getAnimatedValue();

        int drawX = (int)(mSrcX + animeVal * mDeltaX);
        int drawY = (int)(mSrcY + animeVal * mDeltaY);

        // キャンバス（背景）を透過
        // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 画像を描画
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, drawX, drawY, null );
        }
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

    // SurfaceView変更時
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //何もしない
    }

    // SurfaceView終了時
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //何もしない
    }

    // ValueAnimatorを返す。.start()を呼べばアニメーション開始
    // 開始前に、次のsetAnimationInfoを行う必要がある
    public ValueAnimator getmAnimator() {
        return mAnimator;
    }

    // アニメーション開始に必要な情報の設定
    public void setAnimationInfo(Bitmap mBitmap, int mSrcX, int mSrcY, int mDestX, int mDestY) {
        this.mBitmap = mBitmap;
        this.mSrcX = mSrcX;
        this.mSrcY = mSrcY;

        // 移動距離も計算
        this.mDeltaX = mDestX - mSrcX;
        this.mDeltaY = mDestY - mSrcY;
        //Log.d("setAnimationInfo","mSrcX="+mSrcX+",mSrcY="+mSrcY+",mDeltaX="+mDeltaX+",mDeltaY="+mDeltaY);
    }



}
