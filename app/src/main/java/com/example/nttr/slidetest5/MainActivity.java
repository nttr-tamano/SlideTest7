package com.example.nttr.slidetest5;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements SurfaceHolder.Callback,Runnable { //implements View.OnTouchListener {

    // タッチイベントを処理するためのインタフェース
    private GestureDetector mGestureDetector;

    // スライドエリアの縦横のマス数
    private int mPieceX = 4;
    private int mPieceY = 4; // 原則同数にする。異なる場合、各マスが長方形になる

    // レイアウト関連
    private final int PIECE_MARGIN = 15;    // 各マスのマージン。15: mPieceX=4で調整

    // スライドエリアの各パネルをLinearLayoutで管理し、配列化
    //ArrayList<ImageView> mImagePieces = new ArrayList<ImageView>();
    ArrayList<CustomView> mImagePieces = new ArrayList<>();

    // 操作対象マスの管理用(-1は操作対象なし)
    private final int SELECT_NONE = -1;
    private int mPieceTag = SELECT_NONE;

    // フリック方向の定数
    final int DIRECTION_NONE   = 0;
    final int DIRECTION_TOP    = 1;
    final int DIRECTION_LEFT   = 2;
    final int DIRECTION_RIGHT  = 3;
    final int DIRECTION_BOTTOM = 4;

    // SurfaceView関連(Activityに配置済み想定)
    TranslationSurfaceView mSurfaceView;
    SurfaceHolder mHolder;

    // スレッドを用いたアニメーション用
    // Thread mThread;
    private ValueAnimator mAnimator;
    boolean isAttached = true; //false;

    // 画像の表示用
    Bitmap mBitmap;
    CustomView[] cv = new CustomView[4];

//    final int ANIME_NONE = 0;
//    final int ANIME_UP = 3;     // 値はフリック方向と統一すること
//    final int ANIME_LEFT = 4;
//    final int ANIME_RIGHT = 2;
//    final int ANIME_DOWN = 1;

    int mAnimeDirection = DIRECTION_NONE;   // アニメーション方向
    int mAnimeSrcIndex = SELECT_NONE;        // アニメーションの移動元のID
    int mAnimeDestIndex = SELECT_NONE;       // アニメーションの移動先のID

    // アニメーション時間決定用
    // ANIME_FRAME * ANIME_WAIT_MSEC / 1000 [sec] がアニメーション時間
    final int ANIME_FRAME = 12;
    final int ANIME_WAIT_MSEC = 300; // msec

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SurfaceViewの初期設定
        mSurfaceView = (TranslationSurfaceView) findViewById(R.id.surfaceView);
//        mHolder = mSurfaceView.getHolder();
//        // コールバック設定
//        mHolder.addCallback(this);
//        // 透過
//        mHolder.setFormat(PixelFormat.TRANSLUCENT);
//        // 一番手前に表示
//        mSurfaceView.setZOrderOnTop(true);

        // タッチイベントのインスタンスを生成
        mGestureDetector = new GestureDetector(this, mOnGestureListener);

        // Mainが重いので、
        // I/Choreographer: Skipped 1042 frames!  The application may be doing too much work on its main thread.
        // http://mussyu1204.myhome.cx/wordpress/it/?p=5
        (new Thread(new Runnable() {
            @Override
            public void run() {
                //ここで処理時間の長い処理を実行する

                // LinearLayoutの入れ子
                // スライドエリア（親）は配置済
                LinearLayout llSlideArea = (LinearLayout) findViewById(R.id.llSlideArea);
                // 周辺の余白はマージン1個分のため、小さめ。調整するなら、親要素のpaddingの追加が必要か

                for (int i = 0; i < mPieceX; i++) {

                    // 子（1行分のLinearLayout）の生成
                    LinearLayout.LayoutParams lpRow
                            = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0);
                    lpRow.weight = 1.0f;
                    LinearLayout llRow = new LinearLayout(MainActivity.this);
                    llRow.setOrientation(LinearLayout.HORIZONTAL);
                    llRow.setLayoutParams(lpRow);
                    llRow.setGravity(Gravity.CENTER_VERTICAL);

                    llSlideArea.addView(llRow);

                    for (int j = 0; j < mPieceY; j++) {

                        // 孫（各パネル）の生成
                        LinearLayout.LayoutParams lpPiece
                                = new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        lpPiece.setMargins(PIECE_MARGIN, PIECE_MARGIN, PIECE_MARGIN, PIECE_MARGIN);
                        lpPiece.weight = 1.0f;
                        //ImageView iv = new ImageView(this);
                        CustomView iv = new CustomView(MainActivity.this);
                        iv.setVisibility(View.INVISIBLE);
                        // http://blog.lciel.jp/blog/2013/12/16/android-capture-view-image/
                        iv.setDrawingCacheEnabled(true);             // キャッシュを取得する設定にする
                        iv.destroyDrawingCache();                    // キャッシュをクリア
                        iv.setLayoutParams(lpPiece);
                        //iv.setImageResource(R.drawable.hoshi);
                        //iv.setScaleType(ImageView.ScaleType.FIT_XY);
                        //iv.setBackgroundColor(Color.BLUE);

                        // https://akira-watson.com/android/button-array.html
                        //iv.setTag("ImageView-" + String.valueOf(i*mPieceY+j)); // 多分、1次元配列上の添え字
                        iv.setTag(String.valueOf(i * mPieceY + j)); // 多分、1次元配列上の添え字

                        // 親へ追加
                        llRow.addView(iv);

                        // リストに追加
                        mImagePieces.add(iv);

                        Log.d("MainActivity", "mImagePieces id:" + i * mPieceY + j + " added");

                    } //for j

                } //for i

                // 表示設定
                // 表示デモ用に左上(0)だけ表示
                mImagePieces.get(0).setVisibility(View.VISIBLE);
            }

        })).start();

    }

    // タッチイベントにジェスチャー受付を定義
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    // タッチイベントのリスナー
    private final GestureDetector.SimpleOnGestureListener mOnGestureListener
             = new GestureDetector.SimpleOnGestureListener() {

        // フリックイベント
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

            final int SWIPE_MIN_DISTANCE = 50;          // 最小移動距離
            final int SWIPE_THRESHOLD_VELOCITY = 200;   // 最小速度
            final float SLOPE_RATE = 1.1f;              // 斜めと判定されないための比率

            // フリック方向
            int directionX = DIRECTION_NONE;
            int directionY = DIRECTION_NONE;
            int direction  = DIRECTION_NONE;

            int flingedViewIndex = -1;

            try {

                // どのPieceのView上かチェック
                flingedViewIndex = getViewIndex(event1.getX(), event1.getY());
                if (SELECT_NONE < flingedViewIndex && flingedViewIndex < mImagePieces.size()) {
                    mPieceTag = flingedViewIndex;
                } else {
                    return false;
                }
                //　非表示状態のCustomViewだったら処理しない
                if (mImagePieces.get(mPieceTag).getVisibility() != View.VISIBLE) {
                    return false;
                }

                // 移動距離・スピードを出力
                float distance_x = event2.getX() - event1.getX();
                float distance_y = event1.getY() - event2.getY();
                String strX = null;
                String strY = null;

                // 左右確認
                // 開始位置から終了位置の移動距離が指定値より大きい
                // X軸の移動速度が指定値より大きい
                if (distance_x < -SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    strX = "左";
                    directionX = DIRECTION_LEFT;

                // 終了位置から開始位置の移動距離が指定値より大きい
                // X軸の移動速度が指定値より大きい
                } else if (distance_x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    strX = "右";
                    directionX = DIRECTION_RIGHT;

                }

                // Y軸の移動速度が指定値より大きい
                if (distance_y > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    strY = "上";
                    directionY = DIRECTION_TOP;

                } else if (distance_y < -SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    strY = "下";
                    directionY = DIRECTION_BOTTOM;

                }

                // 方向（上記の合成？）
                // event1:移動開始 event2:移動終了
                // X軸は、右が大きい。Y軸は下が大きい。
                if (Math.abs(distance_x) > Math.abs(distance_y) * SLOPE_RATE) {
                    // X方向が大きい
                    Log.d("onFling", mPieceTag + "が" + strX + "方向");
                    direction = directionX;

                } else if (Math.abs(distance_x) * SLOPE_RATE < Math.abs(distance_y)) {
                    // Y方向が大きい
                    Log.d("onFling",mPieceTag + "が" + strY + "方向");
                    direction = directionY;

                } else {
                    // ほぼ等しいは無視扱い
                    Log.d("onFling", "ほぼ、斜め" + strX + strY + "方向");
                    direction = DIRECTION_NONE;

                }

            } catch (Exception e) {
            }

            // 移動先のIDの取得を試みる
            int destViewIndex = getDestViewIndex(flingedViewIndex, direction);

            // 移動不可の場合、SELECT_NONEが返るのでアニメーションしない
            if (destViewIndex == SELECT_NONE) {
                return false;
            }

            Log.d("onFling", "src: "+flingedViewIndex+" dest: "+destViewIndex);

            if (mAnimator == null || !mAnimator.isRunning()) {
                // 移動可能なのでアニメーション準備
                mAnimeDirection = direction;
                mAnimeSrcIndex = flingedViewIndex;
                mAnimeDestIndex = destViewIndex;

                // 移動先に画像情報をセット
                mImagePieces.get(mAnimeDestIndex).setRes(mImagePieces.get(mAnimeSrcIndex).getRes());

                // スレッドの内容の実行許可
                isAttached = true;

                // ホサカさんが、ThreadをValueAnimatorへ置き換えた
                //mAnimator = ValueAnimator.ofFloat(0f,1f);
                mAnimator = mSurfaceView.getmAnimator();

//                // Animator開始終了時の処理を記載
//                // https://qiita.com/mattak/items/aaf699a046b1a86d8b66
//                mAnimator.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });

                // アニメーション時間
                mAnimator.setDuration(1000);
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        //Log.d(MainActivity.class.getSimpleName(), "onAnimationUpdate");
                        // Log.d()が効かない
                        while(isAttached) {
                            final CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);

                            Bitmap bitmap_work = srcImageView.getDrawingCache();   // キャッシュを作成して取得する
                            // ImageViewのサイズに合わせる
                            mBitmap = Bitmap.createScaledBitmap(bitmap_work, srcImageView.getMeasuredWidth(),
                                    srcImageView.getMeasuredHeight(), false);

                            // 描画開始位置
                            int srcX = srcImageView.getLeft();
                            // Y座標は親View(横長LinearLayout)から取得
                            // http://ichitcltk.hustle.ne.jp/gudon2/index.php?pageType=file&id=Android059_ViewTree
                            int srcY = ((View) srcImageView.getParent()).getTop();
                            // Log.d("location", "imageView X=" + srcX + ",Y=" + srcY);

//                            // 移動元ImageViewの画像を非表示にする
//                            srcImageView.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    //srcImageView.setImageBitmap(null);
//                                    srcImageView.setVisibility(View.INVISIBLE);
//                                }
//                            });

//                            // 移動先ImageViewに画像を書き込む
//                            final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
//                            destImageView.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    destImageView.setImageBitmap(mBitmap);
//                                }
//                            });

                            // 移動先座標。switch caseで制御
                            int destX = 0;
                            int destY = 0;
                            int deltaX = 0;
                            int deltaY = 0;

                            switch (mAnimeDirection) {
                                case DIRECTION_TOP:
                                    destX = srcX;
                                    destY = srcY - ((View) srcImageView.getParent()).getHeight();
                                    deltaX = 0;
                                    deltaY = -((View) srcImageView.getParent()).getHeight() / ANIME_FRAME;
                                    break;

                                case DIRECTION_LEFT:
                                    destX = srcX - srcImageView.getWidth();
                                    destY = srcY;
                                    deltaX = -srcImageView.getWidth() / ANIME_FRAME;
                                    deltaY = 0;
                                    break;

                                case DIRECTION_RIGHT:
                                    destX = srcX + srcImageView.getWidth();
                                    destY = srcY;
                                    deltaX = srcImageView.getWidth() / ANIME_FRAME;
                                    deltaY = 0;
                                    break;

                                case DIRECTION_BOTTOM:
                                    destX = srcX;
                                    destY = srcY + ((View) srcImageView.getParent()).getHeight();
                                    deltaX = 0;
                                    deltaY = ((View) srcImageView.getParent()).getHeight() / ANIME_FRAME;
                                    break;
                            }

                            // Customviewの各パネルのマージンの分だけ表示位置を補正
                            // 縦が不要なのは、余白なしの上位のLinearLayoutの座標を使用しているため
                            //srcX += PIECE_MARGIN;
                            srcY += PIECE_MARGIN;
                            //destX += PIECE_MARGIN;
                            destY += PIECE_MARGIN;

                            // アニメーションのループ
                            // int型を比較しており、値の補正を行っているため、座標一致で終了が可能
                            while (srcY != destY || srcX != destX) {

                                // 移動
                                // 左右へ
                                srcX += deltaX;
                                // 上下へ
                                srcY += deltaY;
                                //Log.d("thread","1 srcX="+srcX+" srcY="+srcY);

                                // 移動先を通り過ぎたら、移動先へ補正
                                if (deltaY > 0 && srcY > destY) {           // 下
                                    srcY = destY;
                                } else if ( deltaY < 0 && srcY < destY ) {  // 上
                                    srcY = destY;
                                }
                                if (deltaX > 0 && srcX > destX) {           // 右
                                    srcX = destX;
                                } else if (deltaX < 0 && srcX < destX) {    // 左
                                    srcX = destX;
                                }
                                //Log.d("thread","2 srcX="+srcX+" srcY="+srcY);

                                mSurfaceView.setAnimationInfo(Bitmap,srcX,srcY,deltaX,deltaY);


                                // 次の描画
                                Canvas canvas = mHolder.lockCanvas();
                                // キャンバス（背景）を透過
                                // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
                                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                // 画像を描画
                                Paint p = new Paint();
                                canvas.drawBitmap(mBitmap, srcX, srcY, p);
                                // 描画終了
                                mHolder.unlockCanvasAndPost(canvas);

//                                // http://boco.hp3200.com/game-devs/view/3.html
//                                //ウェイト処理
//                                try {
//                                    Thread.sleep(ANIME_WAIT_MSEC);
//                                } catch (InterruptedException e) {
//                                }

                            }

                            // 移動先ImageViewの表示
                            final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
                            destImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    destImageView.setVisibility(View.VISIBLE);
                                }
                            });

                            // http://boco.hp3200.com/game-devs/view/3.html
                            // アニメーション終了後ウェイト処理
                            try {
                                Thread.sleep(ANIME_WAIT_MSEC);
                            } catch (InterruptedException e) {
                            }

                            // SurfaceViewのクリア
                            Canvas canvas = mHolder.lockCanvas();
                            // キャンバス（背景）を透過
                            // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                            mHolder.unlockCanvasAndPost(canvas);

                            // スレッド終了
                            isAttached = false;
                        }
                    }
                });
            }
            // 移動元ImageViewの画像を非表示にする
            final CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);
            srcImageView.setVisibility(View.INVISIBLE);

            // 移動先ImageViewに画像を書き込む
            final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
            destImageView.setImageBitmap(mBitmap);

            // アニメーション終了後ウェイト処理
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            mAnimator.start();
            /*
            // スレッド排他制御。起動していなければスレッドスタート
            if (mThread == null || mThread.isAlive() == false) {

                // 移動可能なのでアニメーション準備
                mAnimeDirection = direction;
                mAnimeSrcIndex = flingedViewIndex;
                mAnimeDestIndex = destViewIndex;

                // 移動先に画像情報をセット
                mImagePieces.get(mAnimeDestIndex).setRes(mImagePieces.get(mAnimeSrcIndex).getRes());

                // スレッドの内容の実行許可
                isAttached = true;


                // 参考:スレッド生成をOnClick内から呼ぶときは、MainActivity必須か
                mThread = new Thread(MainActivity.this);
                // スレッド開始
                // 1つのスレッドは1回しかstart()できない(java.lang.IllegalThreadStateException: Thread already started)
                // http://blog.codebook-10000.com/entry/20140530/1401450268
                mThread.start();
            }*/
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mAnimator != null) {
            mAnimator.resume();
        }
    }

    // 非表示から復帰の際に落ちることへの対策
    @Override
    protected void onPause() {
        if (mAnimator != null) {
            mAnimator.pause();
        }

        // onPauseではsuperより前に追記した方が良いとのこと（ホサカさん）
        super.onPause();
        /*
        if (mThread != null) {
            while(mThread.isAlive()) {
                try {
                    Thread.sleep(200);
                }
                catch (Exception e) {
                }
            }
        }
        isAttached = false;
        mThread = null; //スレッドを終了
        */
    }

    // フリックの開始位置にあるCustomViewのIDを取得
    private int getViewIndex(float flingX, float flingY) {
        int index = SELECT_NONE;
        //final int padding = 0;

        ImageView iv;

        for (int i=0; i<mImagePieces.size(); i++) {
            iv = mImagePieces.get(i);
            // Viewの座標の取得
            // http://rounin.biz/programming/358/
            // 判定の度に取得してたら重い？もったいない？
            int[] lo = new int[2];
            iv.getLocationInWindow(lo);
            int ivLeft = lo[0];
            int ivTop = lo[1];
            int ivRight = ivLeft + iv.getWidth();
            int ivBottom = ivTop + iv.getHeight();

            // 判定を厳しくする(余白の内側のみにする)
            // ※余白は、Viewのpaddingプロパティにするのもあり
            ivLeft   += PIECE_MARGIN;
            ivTop    += PIECE_MARGIN;
            ivRight  -= PIECE_MARGIN;
            ivBottom -= PIECE_MARGIN;

            // マイナス(矛盾)を補正しないのは、次のif文が成立しないため

            // 引数の座標がこのView内であるか判定
            if (ivLeft < flingX && flingX < ivRight
                    && ivTop < flingY && flingY < ivBottom ) {
                Log.d("onFling", "In ImageView-" + i);
                // Viewが見つかったのでループ中断
                index = i;
                break;
            } //if

        } //for i
        return index;
    }

    // フリックによる移動先のCustomViewのIDを取得(無いかもしれない)
    int getDestViewIndex(int srcIndex,int direction) {

        int destIndex = SELECT_NONE;

        switch(direction) {
            case DIRECTION_TOP:
                // 上方向へ移動
                destIndex = srcIndex - mPieceX;
                if (destIndex < 0) {
                    destIndex = SELECT_NONE;
                }
                break;

            case DIRECTION_LEFT:
                // 左方向へ移動
                if (srcIndex % mPieceX == 0) {
                    destIndex = SELECT_NONE;
                } else {
                    destIndex = srcIndex - 1;
                }
                break;

            case DIRECTION_RIGHT:
                // 右方向へ移動
                if ((srcIndex + 1) % mPieceX == 0) {
                    destIndex = SELECT_NONE;
                } else {
                    destIndex = srcIndex + 1;
                }
                break;

            case DIRECTION_BOTTOM:
                // 下方向へ移動
                destIndex = srcIndex + mPieceX;
                // 要素数以上なので、mImagePieces.size() でも可
                if (destIndex >= mPieceX * mPieceY ) {
                    destIndex = SELECT_NONE;
                }
                break;

        }
        return destIndex;

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

    }

    // SurfaceView終了時
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isAttached = false;
        mAnimator = null;
        // mThread = null; //スレッドを終了
    }

    // 一定時間毎に移動するアニメーション
    @Override
    public void run() {

        // Log.d()が効かない
        while(isAttached) {
            final CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);

            Bitmap bitmap_work = srcImageView.getDrawingCache();   // キャッシュを作成して取得する
            // ImageViewのサイズに合わせる
            mBitmap = Bitmap.createScaledBitmap(bitmap_work, srcImageView.getMeasuredWidth(),
                    srcImageView.getMeasuredHeight(), false);

            // 描画開始位置
            int srcX = srcImageView.getLeft();
            // Y座標は親View(横長LinearLayout)から取得
            // http://ichitcltk.hustle.ne.jp/gudon2/index.php?pageType=file&id=Android059_ViewTree
            int srcY = ((View) srcImageView.getParent()).getTop();
            // Log.d("location", "imageView X=" + srcX + ",Y=" + srcY);

            srcImageView.post(new Runnable() {
                @Override
                public void run() {
                    srcImageView.setVisibility(View.INVISIBLE);
                    //srcImageView.setImageBitmap(null);
                }
            });

            // 移動先ImageViewに画像を書き込む
            final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
            destImageView.post(new Runnable() {
                @Override
                public void run() {
                    destImageView.setImageBitmap(mBitmap);
                }
            });

            // 移動先座標。switch caseで制御
            int destX = 0;
            int destY = 0;
            int deltaX = 0;
            int deltaY = 0;

            switch (mAnimeDirection) {
                case DIRECTION_TOP:
                    destX = srcX;
                    destY = srcY - ((View) srcImageView.getParent()).getHeight();
                    deltaX = 0;
                    deltaY = -((View) srcImageView.getParent()).getHeight() / ANIME_FRAME;
                    break;

                case DIRECTION_LEFT:
                    destX = srcX - srcImageView.getWidth();
                    destY = srcY;
                    deltaX = -srcImageView.getWidth() / ANIME_FRAME;
                    deltaY = 0;
                    break;

                case DIRECTION_RIGHT:
                    destX = srcX + srcImageView.getWidth();
                    destY = srcY;
                    deltaX = srcImageView.getWidth() / ANIME_FRAME;
                    deltaY = 0;
                    break;

                case DIRECTION_BOTTOM:
                    destX = srcX;
                    destY = srcY + ((View) srcImageView.getParent()).getHeight();
                    deltaX = 0;
                    deltaY = ((View) srcImageView.getParent()).getHeight() / ANIME_FRAME;
                    break;

            }

            // Customviewの各パネルのマージンの分だけ表示位置を補正
            // 縦が不要なのは、余白なしの上位のLinearLayoutの座標を使用しているため
            //srcX += PIECE_MARGIN;
            srcY += PIECE_MARGIN;
            //destX += PIECE_MARGIN;
            destY += PIECE_MARGIN;

            // アニメーションのループ
            // int型を比較しており、値の補正を行っているため、座標一致で終了が可能
            while (srcY != destY || srcX != destX) {

                // 移動
                // 左右へ
                srcX += deltaX;
                // 上下へ
                srcY += deltaY;
                //Log.d("thread","1 srcX="+srcX+" srcY="+srcY);

                // 移動先を通り過ぎたら、移動先へ補正
                if (deltaY > 0 && srcY > destY) {           // 下
                    srcY = destY;
                } else if ( deltaY < 0 && srcY < destY ) {  // 上
                    srcY = destY;
                }
                if (deltaX > 0 && srcX > destX) {           // 右
                    srcX = destX;
                } else if (deltaX < 0 && srcX < destX) {    // 左
                    srcX = destX;
                }
                //Log.d("thread","2 srcX="+srcX+" srcY="+srcY);

                // 次の描画
                Canvas canvas = mHolder.lockCanvas();
                // キャンバス（背景）を透過
                // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                // 画像を描画
                Paint p = new Paint();
                canvas.drawBitmap(mBitmap, srcX, srcY, p);
                // 描画終了
                mHolder.unlockCanvasAndPost(canvas);

                // http://boco.hp3200.com/game-devs/view/3.html
                //ウェイト処理
                try {
                    Thread.sleep(ANIME_WAIT_MSEC);
                } catch (InterruptedException e) {
                }

            }

            // 移動先ImageViewの表示
            destImageView.post(new Runnable() {
                @Override
                public void run() {
                    destImageView.setVisibility(View.VISIBLE);
                }
            });

            // http://boco.hp3200.com/game-devs/view/3.html
            // アニメーション終了後ウェイト処理
            try {
                Thread.sleep(ANIME_WAIT_MSEC);
            } catch (InterruptedException e) {
            }

            // SurfaceViewのクリア
            Canvas canvas = mHolder.lockCanvas();
            // キャンバス（背景）を透過
            // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            mHolder.unlockCanvasAndPost(canvas);

            // スレッド終了
            isAttached = false;
        }
        // run()が終わるとスレッドは消滅する
        // http://www.techscore.com/tech/Java/JavaSE/Thread/4-4/
    }
}
