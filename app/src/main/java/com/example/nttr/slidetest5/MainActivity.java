package com.example.nttr.slidetest5;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
    private final int PIECE_MARGIN = 8;    // 各マスのマージン。15: mPieceX=4で調整

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

    final int DIRECTION_TOP_LEFT     = 5;
    final int DIRECTION_TOP_RIGHT    = 6;
    final int DIRECTION_BOTTOM_LEFT  = 7;
    final int DIRECTION_BOTTOM_RIGHT = 8;

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
     private int mVanishColor = R.color.hotpink;

    // 4分割の添え字
    private final int PART_UL = 0; // 左上
    private final int PART_UR = 1; // 右上
    private final int PART_LL = 2; // 左下
    private final int PART_LR = 3; // 右下
    // コード値の組と初期配置先
    int aryImgRes[][] = {
            {SELECT_NONE, SELECT_NONE,
                    SELECT_NONE,3,4},
            {SELECT_NONE, SELECT_NONE,
                    2, SELECT_NONE,7},
            {SELECT_NONE, 1,
                    SELECT_NONE,SELECT_NONE,8},
            {0, SELECT_NONE,
                    SELECT_NONE,SELECT_NONE,9},
    };

    // コード値をリソースIDへ変換する定数配列っぽいクラス
    private CodeToResource c2r = new CodeToResource();
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
//        int aryImgRes[][] = {
//                {R.drawable.arrow_left,CustomView.UNDEFINED_RESOURCE,
//                        CustomView.UNDEFINED_RESOURCE,R.drawable.arrow_right,0},
//                {CustomView.UNDEFINED_RESOURCE,R.drawable.arrow_left,
//                        R.drawable.arrow_right,CustomView.UNDEFINED_RESOURCE,10}
//        };

        // ピンク星
//        int aryImgRes[][] = {
//                {CustomView.UNDEFINED_RESOURCE, CustomView.UNDEFINED_RESOURCE,
//                        CustomView.UNDEFINED_RESOURCE,R.drawable.pink_star_ul,4},
//                {CustomView.UNDEFINED_RESOURCE, R.drawable.pink_star_ur,
//                        CustomView.UNDEFINED_RESOURCE, CustomView.UNDEFINED_RESOURCE,7},
//                {CustomView.UNDEFINED_RESOURCE, CustomView.UNDEFINED_RESOURCE,
//                        R.drawable.pink_star_ll,CustomView.UNDEFINED_RESOURCE,8},
//                {R.drawable.pink_star_lr, CustomView.UNDEFINED_RESOURCE,
//                        CustomView.UNDEFINED_RESOURCE,CustomView.UNDEFINED_RESOURCE,9},
//        };
//        aryImgRes[][]


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
            int resId;
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 2; i++) {
                    int BitmapId = i + j * 2;
                    if (aryImgRes[k][BitmapId] != SELECT_NONE) {
                        // コードをリソースIDへ変換
                        resId = c2r.getResID(aryImgRes[k][BitmapId]);
                        // Bitmapをリソースから読み込む
                        bitmapWork1 = BitmapFactory.decodeResource(resources, resId);
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
            destImageView.setVisibility(View.VISIBLE);
            mBitmapList.add(bitmapBase);
        }

//        // 表示デモ用に左上(0)だけ表示
//        mImagePieces.get(0).setVisibility(View.VISIBLE);
////                // 画像を掲載
////                // https://www.javadrive.jp/start/array/index5.html
////                mImagePieces.get(0).setRes(imgRes);
//
//        // 通り抜け防止テストで、10も追加
//        mImagePieces.get(10).setVisibility(View.VISIBLE);

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
            int destViewIndex = getDestViewIndex(flingedViewIndex, direction, true);

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

                    // １回実行済みであれば再実行不要
                    // 移動先CustomViewが見えていれば、1回実行済みと見なす
                    if (destImageView.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    //destImageView.post(new Runnable() {
                    //    @Override
                    //    public void run() {
                            destImageView.setVisibility(View.VISIBLE);
                    //    }
                    //});

                    // 模様の成立チェック
                    int resID = destImageView.getResID();
                    // 最低2箇所チェックする必要がある
                    boolean flag1 = false;
                    boolean flag2 = false;

                    // フリック方向に基づき、チェック対象を決定
                    switch (mAnimeDirection) {
                        case DIRECTION_TOP:
                            // 左上、右上
                            flag1 = checkMatch(resID,PART_UL);
                            flag2 = checkMatch(resID,PART_UR);

                            // 消去
                            if (flag1 == true) {
                                vanishMatch(PART_UL);
                            }
                            if (flag2 == true) {
                                vanishMatch(PART_UR);
                            }
                            break;
                        case DIRECTION_LEFT:
                            // 左上、左下
                            flag1 = checkMatch(resID,PART_UL);
                            flag2 = checkMatch(resID,PART_LL);

                            // 消去
                            if (flag1 == true) {
                                vanishMatch(PART_UL);
                            }
                            if (flag2 == true) {
                                vanishMatch(PART_LL);
                            }

                            break;
                        case DIRECTION_RIGHT:
                            // 右上、右下
                            flag1 = checkMatch(resID,PART_UR);
                            flag2 = checkMatch(resID,PART_LR);

                            // 消去
                            if (flag1 == true) {
                                vanishMatch(PART_UR);
                            }
                            if (flag2 == true) {
                                vanishMatch(PART_LR);
                            }
                            break;
                        case DIRECTION_BOTTOM:
                            // 左下、右下
                            flag1 = checkMatch(resID,PART_LL);
                            flag2 = checkMatch(resID,PART_LR);

                            // 消去
                            if (flag1 == true) {
                                vanishMatch(PART_LL);
                            }
                            if (flag2 == true) {
                                vanishMatch(PART_LR);
                            }
                            break;
                    }
                    Log.d("check","mAnimeDirection="+mAnimeDirection
                            +",flag1="+flag1+",flag2="+flag2);


//                    if (mAnimeDirection == DIRECTION_TOP || mAnimeDirection == DIRECTION_LEFT) {
//                        //TODO
//                    }
//
//                    // 右上を基準に、その右・上・右上をチェック
//                    //TODO
//
//                    Log.d("check","mAnimeDirection="+mAnimeDirection);
//
//                    // 左下を基準に、その左・下・左下をチェック
//                    if (mAnimeDirection == DIRECTION_BOTTOM || mAnimeDirection == DIRECTION_LEFT) {
//                    }

//                    while (mAnimeDirection == DIRECTION_BOTTOM || mAnimeDirection == DIRECTION_LEFT) {
//
//                        // 正否判定用
//                        boolean flagMatch = true; // マッチしている(可能性がある)=true、ない=false
//
//                        // 基準位置のコードを取得
//                        int resID = destImageView.getResID();
//                        int code = aryImgRes[resID][PART_LL];
//                        int codeGroup = code / 4; // 当面は4つマッチとする
//
//                        Log.d("check","resID="+resID+",code="+code);
//
//                        // SELECT_NONEでなければ、周辺のコードを取得
//                        if (code != SELECT_NONE) {
//
//                            // チェック対象となる位置の情報
//                            // 左の、右下
//                            // 下の、左上
//                            // 左下の、右上
//                            int directions[] = {DIRECTION_LEFT, DIRECTION_BOTTOM, DIRECTION_BOTTOM_LEFT};
//                            int positions[] = {PART_LR,PART_UL,PART_UR};
//
//                            for (int i = 0; i < directions.length; i++) {
//                                int targetCode = getAroundCode(mAnimeDestIndex, directions[i], positions[i]);
//                                Log.d("check","targetCode="+targetCode);
//                                // マッチしないまたはSELECT_NONEあれば判定終了
//                                if ( targetCode == SELECT_NONE || (targetCode / 4) != codeGroup) {
//                                    flagMatch = false;
//                                    break;
//                                }
//                            }
//
//                        } else {
//                            flagMatch = false;
//                        }
//                        // マッチしていないことが確定していたらループを中断
//                        if (flagMatch == false) {
//                            break;
//                        }
//
//                        Log.d("check","4 pieces matched at "+mAnimeDestIndex+" !");
//
//                        // 画像を消す処理
//
//
//                        // ループは1回で終了
//                        break;
//                    }

                    // 右下を基準に、その右・下・右下をチェック
                    //TODO

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

    // 基本位置の部位に対応するポジション(チェック対象の方向(相対位置)・部位の組)
    private class Positions {
        private int[] directions = null;        // 方向
        private int[] parts = null;             // 部位
        private int basePart = SELECT_NONE;     // コンストラクタ生成時の引数

        // コンストラクタ
        // 有効な引数毎に決まったメンバ配列を生成する
        public Positions(int basePart) {
            switch(basePart) {
                case PART_UL:
                    // 基準マスの、左上 に対し、
                    // チェック対象となる位置の情報
                    // 上の、左下
                    // 左の、右上
                    // 左上の、右下
                    directions = new int[]{DIRECTION_TOP, DIRECTION_LEFT, DIRECTION_TOP_LEFT};
                    parts = new int[]{PART_LL, PART_UR, PART_LR};
                    break;
                case PART_UR:
                    // 基準マスの、右上 に対し、
                    // チェック対象となる位置の情報
                    // 上の、右下
                    // 右の、左上
                    // 右上の、左下
                    directions = new int[]{DIRECTION_TOP, DIRECTION_RIGHT, DIRECTION_TOP_RIGHT};
                    parts = new int[]{PART_LR, PART_UL, PART_LL};
                    break;
                case PART_LL:
                    // 基準マスの、左下 に対し、
                    // チェック対象となる位置の情報
                    // 下の、左上
                    // 左の、右下
                    // 左下の、右上
                    directions = new int[]{DIRECTION_BOTTOM, DIRECTION_LEFT, DIRECTION_BOTTOM_LEFT};
                    parts = new int[]{PART_UL, PART_LR, PART_UR};
                    break;
                case PART_LR:
                    // 基準マスの、右下 に対し、
                    // チェック対象となる位置の情報
                    // 下の、右上
                    // 右の、左下
                    // 右下の、左上
                    directions = new int[]{DIRECTION_BOTTOM, DIRECTION_RIGHT, DIRECTION_BOTTOM_RIGHT};
                    parts = new int[]{PART_UR,PART_UL,PART_UL};
                    break;
                default:
                    return;
            }
            this.basePart = basePart;
        }

        // 有効な要素数を返す
        public int getSize() {
            if (basePart != SELECT_NONE) {
                return Math.min(directions.length, parts.length);
            } else {
                return SELECT_NONE;
            }
        }

        // 指定の方向を返す
        public int getDirection(int idx) {
            return directions[idx];
        }

        // 指定の部位を返す
        public int getPart(int idx) {
            return parts[idx];
        }
    }

    // 模様の成立をチェックする
    private boolean checkMatch(int resID, int part) {
        int code = aryImgRes[resID][part];
        // 配列の宣言と初期化を別々に行う
        // http://blog.goo.ne.jp/xypenguin/e/e1cfcc0b1a8c3acdbe023bbef8944dac
        //int[] directions = null;    // = new int[3];
        //int[] positions = null;     // = new int[3];

        // チェック対象をクラスで生成
        Positions positions = new Positions(part);
        // ポジションが取得できなかったら不成立
        if (positions.getSize() == SELECT_NONE) {
            return false;
        }

        // 正否判定用
        boolean flagMatch = true; // マッチしている(可能性がある)=true、いない=false

        // 基準位置のコードを取得
        int codeGroup = code / 4; // 当面は4つマッチとする
        //Log.d("check","resID="+resID+",code="+code);

        // SELECT_NONEでなければ、周辺のコードを取得
        if (code != SELECT_NONE) {
            for (int i = 0; i < positions.getSize(); i++) {
                int targetCode = getAroundCode(mAnimeDestIndex,
                        positions.getDirection(i), positions.getPart(i));
                //Log.d("check","targetCode="+targetCode);
                // マッチしないまたはSELECT_NONEあれば判定終了
                if ( targetCode == SELECT_NONE || (targetCode / 4) != codeGroup) {
                    flagMatch = false;
                    return flagMatch;
                }
            }

        } else {
            flagMatch = false;
        }
        // マッチしていないことが確定していたらループを中断
        if (flagMatch == false) {
            return flagMatch;
        }
        Log.d("check","4 pieces matched at "+mAnimeDestIndex+" !");
        return flagMatch;
    }

    // 周辺の指定位置の画像コードを返す
    private int getAroundCode(int srcViewIndex,int direction,int position) {
        int targetViewIndex;
        int targetResID;
        int targetCode;
        // srcViewIndexのCustomViewから見て、direction方向にあるCustomViewのIDを取得
        targetViewIndex = getDestViewIndex(srcViewIndex,direction,false);
        if (targetViewIndex == SELECT_NONE) {
            //Log.d("getAroundCode","targetViewIndex=SELECT_NONE");
            return SELECT_NONE;
        }
        // CustomViewの保持する画像リソースIDを取得
        targetResID = mImagePieces.get(targetViewIndex).getResID();
        if (targetResID == SELECT_NONE) {
            //Log.d("getAroundCode","targetResID=SELECT_NONE");
            return SELECT_NONE;
        }
        // 画像リソースのposition位置のコードを取得
        targetCode = aryImgRes[targetResID][position];
        return targetCode;
    }

    private boolean vanishMatch(int part) {

        // チェック対象をクラスで生成
        Positions positions = new Positions(part);
        // ポジションが取得できなかったら不成立
        if (positions.getSize() == SELECT_NONE) {
            return false;
        }

        // 各部位の画像消去
        for (int i = 0; i < positions.getSize(); i++) {
            vanishImage(mAnimeDestIndex,
                    positions.getDirection(i), positions.getPart(i), SELECT_NONE);
        }
        vanishImage(mAnimeDestIndex, DIRECTION_NONE, part, SELECT_NONE);
        return true;
    }

    // 指定位置の画像コードをtargetCodeで更新し、
    // CustomViewの画像を更新する
    //課題: 消す場合は、SELECT_NONEでないコードと画像を設定するべき？
    @SuppressLint("ResourceAsColor")
    private int vanishImage(int srcViewIndex, int direction, int part, int targetCode) {
        int targetViewIndex;
        int targetResID;
        //int targetCode;
        // srcViewIndexのCustomViewから見て、direction方向にあるCustomViewのIDを取得
        if (direction == DIRECTION_NONE) {
            // 方向なしは、引数自身
            targetViewIndex = srcViewIndex;
        } else {
            // 指定された方向から、IDを探す
            targetViewIndex = getDestViewIndex(srcViewIndex, direction, false);
        }
        // 見つからなかったら終了
        if (targetViewIndex == SELECT_NONE) {
            //Log.d("getAroundCode","targetViewIndex=SELECT_NONE");
            return SELECT_NONE;
        }
        // CustomViewの保持する画像リソースIDを取得
        targetResID = mImagePieces.get(targetViewIndex).getResID();
        if (targetResID == SELECT_NONE) {
            //Log.d("getAroundCode","targetResID=SELECT_NONE");
            return SELECT_NONE;
        }
        // 画像リソースのpart位置のコードを書き換える
        aryImgRes[targetResID][part] = targetCode;

        Log.d("vanish","targetResID="+targetResID);

        // 画像初期設定のパクリ（ここから）

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

        // 画像更新位置の決定
        int bitmapTop = 0;
        int bitmapLeft = 0;
        switch (part) {
            case PART_UL:
                bitmapTop = 0;
                bitmapLeft = 0;
                break;
            case PART_UR:
                bitmapTop = 0;
                bitmapLeft = viewWidthHalf;
                break;
            case PART_LL:
                bitmapTop = viewHeightHalf;
                bitmapLeft = 0;
                break;
            case PART_LR:
                bitmapTop = viewHeightHalf;
                bitmapLeft = viewWidthHalf;
                break;
        }

        // http://cheesememo.blog39.fc2.com/blog-entry-740.html
        // https://developer.android.com/reference/android/graphics/Bitmap.Config.html
        Resources resources = getResources();

        //Bitmap bitmapBase = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        //Canvas canvas = new Canvas(bitmapBase);
        //canvas.drawColor(mBackgroundColor); // 背景色を指定
        Canvas canvas = new Canvas(mBitmapList.get(targetResID));

        // コードをリソースIDへ変換
        int resId = c2r.getResID(aryImgRes[targetResID][part]);
        // Bitmapをリソースから読み込む
        Bitmap bitmapWork1;
        Bitmap bitmapWork2;
        if (aryImgRes[targetResID][part] != SELECT_NONE) {
            bitmapWork1 = BitmapFactory.decodeResource(resources, resId);
            // サイズ補正（AccBall参照）
            bitmapWork2 = Bitmap.createScaledBitmap(bitmapWork1,
                    viewWidthHalf, viewHeightHalf, false);
        } else {
            // 消えた後の色だけの画像を生成
            bitmapWork2 = Bitmap.createBitmap(viewWidthHalf, viewHeightHalf, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = new Canvas(bitmapWork2);
            // 定義した色を使用
            // http://furudate.hatenablog.com/entry/2013/06/19/010953
            canvas2.drawColor(resources.getColor(mVanishColor));
        }
        // 用意した画像を指定の位置へ追加
        canvas.drawBitmap(bitmapWork2,
                bitmapLeft, bitmapTop, (Paint) null);

//        Bitmap bitmapWork1;
//        Bitmap bitmapWork2;
//        int resId;
//        for (int j = 0; j < 2; j++) {
//            for (int i = 0; i < 2; i++) {
//                int BitmapId = i + j * 2;
//                if (aryImgRes[k][BitmapId] != SELECT_NONE) {
//                    // コードをリソースIDへ変換
//                    resId = c2r.getResID(aryImgRes[k][BitmapId]);
//                    // Bitmapをリソースから読み込む
//                    bitmapWork1 = BitmapFactory.decodeResource(resources, resId);
//                    // サイズ補正（AccBall参照）
//                    bitmapWork2 = Bitmap.createScaledBitmap(bitmapWork1,
//                            viewWidthHalf, viewHeightHalf, false);
//                    // View上に描画
//                    canvas.drawBitmap(bitmapWork2,
//                            viewWidthHalf * i, viewHeightHalf * j, (Paint)null);
//                }
//            }
//        }

        // 該当CustomViewへ画像を設置
        //CustomView destImageView = mImagePieces.get(aryImgRes[k][4]);
        CustomView destImageView = mImagePieces.get(targetViewIndex);
        //destImageView.setImageBitmap(bitmapBase);
        //destImageView.setImageBitmap(bitmapWork2);
        destImageView.setImageBitmap(mBitmapList.get(targetResID));
        //destImageView.setResID(k);
        //destImageView.setVisibility(View.VISIBLE);
        //mBitmapList.add(bitmapBase);
        //mBitmapList.set(targetResID,bitmapWork2); // 置き換え
        // 画像初期設定のパクリ（ここまで）

        return targetCode;
    }

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

    // フリックによる移動先のCustomViewのIDを取得(無いかもしれない) (flagMove=true)
    // あるいは、単に指定方向のCustomViewのIDを取得(無いかもしれない) (flagMove=false)
    private int getDestViewIndex(int srcIndex,int direction, boolean flagMove) {

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

            case DIRECTION_TOP_LEFT:
                // 上方向へ移動
                destIndex = srcIndex - mPieceX;
                if (destIndex < 0) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、左方向へ移動
                    if (destIndex % mPieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex - 1;
                    }
                }
                break;

            case DIRECTION_TOP_RIGHT:
                // 上方向へ移動
                destIndex = srcIndex - mPieceX;
                if (destIndex < 0) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、右方向へ移動
                    if ((destIndex + 1) % mPieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex + 1;
                    }
                }
                break;

            case DIRECTION_BOTTOM_LEFT:
                // 下方向へ移動
                destIndex = srcIndex + mPieceX;
                // 要素数以上なので、mImagePieces.size() でも可
                if (destIndex >= mPieceX * mPieceY ) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、左方向へ移動
                    if (destIndex % mPieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex - 1;
                    }
                }
                break;

            case DIRECTION_BOTTOM_RIGHT:
                // 下方向へ移動
                destIndex = srcIndex + mPieceX;
                // 要素数以上なので、mImagePieces.size() でも可
                if (destIndex >= mPieceX * mPieceY ) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、右方向へ移動
                    if ((destIndex + 1) % mPieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex + 1;
                    }
                }
                break;

        }
        // 移動不可確定、または、移動先を返す
        if (destIndex == SELECT_NONE || flagMove == false) {
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
