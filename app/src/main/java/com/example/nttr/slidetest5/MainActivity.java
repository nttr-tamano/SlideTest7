package com.example.nttr.slidetest5;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

public class MainActivity extends AppCompatActivity {
        //implements SurfaceHolder.Callback,Runnable { //implements View.OnTouchListener {

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
    //CustomView[] cv = new CustomView[4];
    //final int UNDEFINED_RESOURCE = 0;        // CustomViewと共通
    private int mBackgroundColor = Color.CYAN;
    ArrayList<Bitmap> mBitmapList = new ArrayList<>();
    int mBitmapID = SELECT_NONE;
    boolean flagSetBitmap = false;  // 処理を1回だけ実行するためのフラグ

    int mAnimeDirection = DIRECTION_NONE;    // アニメーション方向
    int mAnimeSrcIndex = SELECT_NONE;        // アニメーションの移動元のID
    int mAnimeDestIndex = SELECT_NONE;       // アニメーションの移動先のID

    final int ANIME_DURATION = 300;

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

        // ホサカさんが、ThreadをValueAnimatorへ置き換えた
        //mAnimator = ValueAnimator.ofFloat(0f,1f);
        // onCreateに移動したら再表示で落ちなくなった
        mAnimator = mSurfaceView.getmAnimator();

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
                        //iv.setVisibility(View.VISIBLE);   // デバッグ用。最初から表示
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

            }

        })).start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (flagSetBitmap) {
            return;
        }

        // 表示設定
        // ビットマップ情報の定義（固定値版）
        int aryImgRes[][] = {
                {R.drawable.arrow_left,CustomView.UNDEFINED_RESOURCE,
                        CustomView.UNDEFINED_RESOURCE,R.drawable.arrow_right,0},
                {CustomView.UNDEFINED_RESOURCE,R.drawable.arrow_left,
                        R.drawable.arrow_right,CustomView.UNDEFINED_RESOURCE,10}
        };

        // 表示するビットマップ群の定義
        // ビュー内の4箇所に、配置すべき画像情報を加工して配置したビットマップを生成
        // CustomViewは全部同じサイズのはずなので、サンプルとして左上1個を持ってくる
        CustomView sampleView = mImagePieces.get(0);
        // OnCreateではゼロになる
        // https://qiita.com/m1takahashi/items/6fa49b9e44f4ab5c4055
        // http://shim0mura.hatenadiary.jp/entry/2016/01/11/013000
        // http://y-anz-m.blogspot.jp/2010/01/android-view.html
        int viewWidth = sampleView.getWidth();
        int viewHeight = sampleView.getHeight();
        int viewWidthHalf = viewWidth / 2;
        int viewHeightHalf = viewHeight / 2;
        // http://cheesememo.blog39.fc2.com/blog-entry-740.html
        // https://developer.android.com/reference/android/graphics/Bitmap.Config.html

        Resources resources = getResources();

        // https://www.javadrive.jp/start/array/index9.html
        // imgRes.length は、2次元配列の第1要素の長さ
        // imgRes[0].length は、2次元配列の1個目の要素の第2要素の長さ
        for (int k = 0; k < aryImgRes.length; k++) {
            Bitmap bitmapBase = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapBase);
            canvas.drawColor(mBackgroundColor); // 背景色を指定

            Bitmap bitmapWork1;
            Bitmap bitmapWork2;
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 2; i++) {
                    int BitmapId = i * 2 + j;
                    if (aryImgRes[k][BitmapId] != CustomView.UNDEFINED_RESOURCE) {
                        // Bitmapをリソースから読み込む
                        bitmapWork1 = BitmapFactory.decodeResource(resources, aryImgRes[k][BitmapId]);
                        // サイズ補正（AccBall参照）
                        bitmapWork2 = Bitmap.createScaledBitmap(bitmapWork1,
                                viewWidthHalf, viewHeightHalf, false);
                        // View上に描画
                        canvas.drawBitmap(bitmapWork2,
                                viewWidthHalf * i, viewHeightHalf * j, (Paint)null);
                    }
                }
            }

            // 該当CustomViewへ画像を設置
            CustomView destImageView = mImagePieces.get(aryImgRes[k][4]);
            destImageView.setImageBitmap(bitmapBase);
            destImageView.setResID(k);
            mBitmapList.add(bitmapBase);
        }

        // 表示デモ用に左上(0)だけ表示
        mImagePieces.get(0).setVisibility(View.VISIBLE);
//                // 画像を掲載
//                // https://www.javadrive.jp/start/array/index5.html
//                mImagePieces.get(0).setRes(imgRes);

        // 通り抜け防止テストで、10も追加
        mImagePieces.get(10).setVisibility(View.VISIBLE);

        // 再実行しない
        flagSetBitmap = true;
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

            // onDrawを受け取る設定
            // https://developer.android.com/reference/android/view/View.html#setWillNotDraw%28boolean%29
            // ActivityのOnCreate()では早すぎて、ぬるぽ
            mSurfaceView.setWillNotDraw(false);

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
                //mImagePieces.get(mAnimeDestIndex).setRes(mImagePieces.get(mAnimeSrcIndex).getRes());


                // スレッドの内容の実行許可
                isAttached = true;

//                // ホサカさんが、ThreadをValueAnimatorへ置き換えた
//                //mAnimator = ValueAnimator.ofFloat(0f,1f);
//                mAnimator = mSurfaceView.getmAnimator();

                // アニメーション時間
                mAnimator.setDuration(ANIME_DURATION);

                // 移動元の画像の取得
                CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);
//                srcImageView.destroyDrawingCache(); // キャッシュのクリア
//                Bitmap bitmap_work = srcImageView.getDrawingCache();  // 作り直されたキャッシュを取得する

                mBitmapID = srcImageView.getResID();
                if (mBitmapID == SELECT_NONE) {
                    return false;
                }
                mBitmap = mBitmapList.get(mBitmapID);

//                // ImageViewのサイズに合わせる
//                mBitmap = Bitmap.createScaledBitmap(bitmap_work, srcImageView.getMeasuredWidth(),
//                        srcImageView.getMeasuredHeight(), false);


                // 描画開始位置
                int srcX = srcImageView.getLeft();
                // Y座標は親View(横長LinearLayout)から取得
                // http://ichitcltk.hustle.ne.jp/gudon2/index.php?pageType=file&id=Android059_ViewTree
                int srcY = ((View) srcImageView.getParent()).getTop();
                // Log.d("location", "imageView X=" + srcX + ",Y=" + srcY);

                // Customviewの各パネルのマージンの分だけ表示位置を補正
                // 縦が不要なのは、余白なしの上位のLinearLayoutの座標を使用しているため
                //srcX += PIECE_MARGIN;                   // マージン補正
                srcY += PIECE_MARGIN;                   // マージン補正

                // 移動先座標。switch caseで制御
                int destX = 0;
                int destY = 0;
//                int deltaX = 0;
//                int deltaY = 0;

                // X方向・Y方向の移動距離
                switch (mAnimeDirection) {
                    case DIRECTION_TOP:
                        destX = srcX;
                        destY = srcY - ((View) srcImageView.getParent()).getHeight();
                        //destY -= PIECE_MARGIN * 0;      // マージン補正
//                        deltaX = 0;
//                        deltaY = -((View) srcImageView.getParent()).getHeight();
                        break;

                    case DIRECTION_LEFT:
                        destX = srcX - srcImageView.getWidth();
                        destX -= PIECE_MARGIN * 2;      // マージン補正
                        destY = srcY;
//                        deltaX = -srcImageView.getWidth();
//                        deltaY = 0;
                        break;

                    case DIRECTION_RIGHT:
                        destX = srcX + srcImageView.getWidth();
                        destX += PIECE_MARGIN * 2;      // マージン補正
                        destY = srcY;
//                        deltaX = srcImageView.getWidth();
//                        deltaY = 0;
                        break;

                    case DIRECTION_BOTTOM:
                        destX = srcX;
                        destY = srcY + ((View) srcImageView.getParent()).getHeight();
                        //destY += PIECE_MARGIN * 0;      // マージン補正
//                        deltaX = 0;
//                        deltaY = ((View) srcImageView.getParent()).getHeight();
                        break;
                }

                //srcX += PIECE_MARGIN;
//                srcY += PIECE_MARGIN;
                //destX += PIECE_MARGIN;
//                destY += PIECE_MARGIN;

                // アニメーション情報を設定
                mSurfaceView.setAnimationInfo(mBitmap,srcX,srcY,destX,destY);

//                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        //Log.d(MainActivity.class.getSimpleName(), "onAnimationUpdate");
//                        // Log.d()が効かない
//                        while(isAttached) {
//
//
//
////                            // 移動先ImageViewの表示
////                            final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
////                            destImageView.post(new Runnable() {
////                                @Override
////                                public void run() {
////                                    destImageView.setVisibility(View.VISIBLE);
////                                }
////                            });
////
////                            // http://boco.hp3200.com/game-devs/view/3.html
////                            // アニメーション終了後ウェイト処理
////                            try {
////                                Thread.sleep(ANIME_WAIT_MSEC);
////                            } catch (InterruptedException e) {
////                            }
////
////                            // SurfaceViewのクリア
////                            Canvas canvas = mHolder.lockCanvas();
////                            // キャンバス（背景）を透過
////                            // https://qiita.com/androhi/items/a1ed36d3743d5b8cb771
////                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
////                            mHolder.unlockCanvasAndPost(canvas);
//
//                            // スレッド終了
//                            isAttached = false;
//                        }
//                    }
//                });
            }

            // アニメーション開始前・終了後の処理をコールバックに追加
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    // 移動元ImageViewの画像を非表示にする
                    final CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);
                    srcImageView.setVisibility(View.INVISIBLE);
                    srcImageView.setResID(SELECT_NONE);

                    // 移動先ImageViewに画像を書き込む
                    final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
                    //destImageView.setImageBitmap(mBitmap);
//                    destImageView.setRes(srcImageView.getRes());
                    destImageView.setImageBitmap(mBitmap);
                    destImageView.setResID(mBitmapID);

                    // 配列のデバッグ出力
                    // http://akisute3.hatenablog.com/entry/20120204/1328346655
                    //Log.d("onAnimeStart","src="+mAnimeSrcIndex+",Res="+Arrays.toString(srcImageView.getRes()));
                    //Log.d("onAnimeStart","dest"+mAnimeDestIndex+",Res="+Arrays.toString(destImageView.getRes()));

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
                    //destImageView.post(new Runnable() {
                    //    @Override
                    //    public void run() {
                            destImageView.setVisibility(View.VISIBLE);
                    //    }
                    //});
                    super.onAnimationEnd(animation);
                }
            });
            mAnimator.start();

            // 移動先ImageViewの表示
//            //final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
//            destImageView.post(new Runnable() {
//                @Override
//                public void run() {
//                    destImageView.setVisibility(View.VISIBLE);
//                }
//            });

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
            //Log.d("SurfaceView", "Alpha="+mSurfaceView.getAlpha());
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
    private int getDestViewIndex(int srcIndex,int direction) {

        int destIndex = SELECT_NONE;

        //////////////////////////////
        // フリック方向別に算出
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
        // 移動不可確定
        if (destIndex == SELECT_NONE) {
            return destIndex;
        }

        //////////////////////////////
        // 移動先にもパネルが在るかを判定
        // 180205 当面はVISIBLEなら在ると見なす
        int visible = mImagePieces.get(destIndex).getVisibility();
        if (visible == View.VISIBLE) {
            destIndex = SELECT_NONE;
        }

        return destIndex;

    }

}
