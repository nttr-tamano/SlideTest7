package com.example.nttr.slidetest7;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
        //implements SurfaceHolder.Callback,Runnable { //implements View.OnTouchListener {

    // タッチイベントを処理するためのインタフェース
    private GestureDetector mGestureDetector;

    // Intent
    final int INTENT_MODE_TRIAL   = 0;
    final int INTENT_MODE_EASY    = 1;
    final int INTENT_MODE_SURVIVAL = 2;
    final int INTENT_MODE_HARD    = 3;
    Intent mIntent;

    // アクティビティ生成時の引数
    // スライドエリアの縦横のマス数
    //private int mPieceX = 4;
    //private int mPieceY = 4; // 原則同数にする。異なる場合、各マスが長方形になる
    //private int mMode = 0;

    // ステージ管理系
    // Realm
    Realm mRealm;
    // ステージを構成する全情報
    PlayInfo mPlayInfo;    // プレイ中の全データを持ち、Realmへ格納可能なクラス(ほぼ構造体)
    //private int mStageNumber = 0; // 初期値は0。loadNewStageを呼ぶと1以上になる
    Button mBtnNextStage;   // 次へボタン
    boolean flagFinalStage = false;
    // さばいばる
    ArrayList<PatternParts> listPP = new ArrayList<>(); // 部位リスト
    final int COLOR_REPEAT = 3; // 同部位の個数。最低値1

    // 点数管理系
    //private int mMoveCount = 0; //test: 9999;
    //private int mVanishCount = 0; //test: 999;
    final int SCORE_VANISH_1 = 10; // 1組模様を消した点数
    final int SCORE_VANISH_2 =  5; // 2組同時消ししたボーナス
    final int SCORE_CLEAR = 10; //50;
    //private int mScore = 0; //test: 99999;
    TextView textScore;
    TextView textExplain;

    // レイアウト関連
    private final int PIECE_MARGIN = 8;    // 各マスのマージン。mPlayInfo.pieceX=4で調整

    // スライドエリアの各パネルをCustomViewで動的に生成し、LinearLayoutで格子状に配置するが、
    // 管理はリストで行う
    //ArrayList<ImageView> mImagePieces = new ArrayList<ImageView>();
    ArrayList<CustomView> mImagePieces = new ArrayList<>();

    // 操作対象パネル・部位等、諸々の管理用
    private final int SELECT_NONE = -1;     // 操作対象なし
    private final int SELECT_NONE2 = -2;    // 模様が消えた後。色以外はSELECT_NONEと同じ

    //private int mPieceTag = SELECT_NONE;

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
    //SurfaceHolder mHolder;

    // スレッドを用いたアニメーション用
    // Thread mThread;
    private ValueAnimator mAnimator;
    //boolean isAttached = true; //false;

    // 画像の表示用
    Bitmap mBitmap;
    //CustomView[] cv = new CustomView[4];
    //final int UNDEFINED_RESOURCE = 0;        // CustomViewと共通
    private int mBackgroundColor = R.color.skyblue;
    private int mVanishColor = R.color.lightpink;

    // 4分割の部位の添え字(aryImgRes[][]の第2要素の添え字の一部
    private final int PART_UL = 0; // 左上
    private final int PART_UR = 1; // 右上
    private final int PART_LL = 2; // 左下
    private final int PART_LR = 3; // 右下

    // コード値の組と、初期配置先CustomViewの添え字
    int aryImgRes[][];

    // コード値をリソースIdへ変換する定数配列っぽいクラス
    private CodeToResource c2r = new CodeToResource();
    // CustomViewやValueAnimatorで表示する画像の配列
    ArrayList<Bitmap> mBitmapList = new ArrayList<>();
    int mBitmapId = SELECT_NONE;
    boolean flagSetBitmap = false;  // 処理を1回だけ実行するためのフラグ

    int mAnimeDirection = DIRECTION_NONE;    // アニメーション方向
    int mAnimeSrcIndex = SELECT_NONE;        // アニメーションの移動元のId
    int mAnimeDestIndex = SELECT_NONE;       // アニメーションの移動先のId

    int mAnimeSrcCenterX = 0;
    int mAnimeSrcCenterY = 0;
    int mAnimeDestCenterX = 0;
    int mAnimeDestCenterY = 0;
    boolean flagAnimeReady = false;

    final int ANIME_DURATION = 100; //200; //300;

    // アニメーション時間決定用
    // ANIME_FRAME * ANIME_WAIT_MSEC / 1000 [sec] がアニメーション時間
    //final int ANIME_FRAME = 12;
    //final int ANIME_WAIT_MSEC = 300; // msec

    // 中断の管理
    static final int RETURN_YES = 1;
    static final int RETURN_NO  = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String INTENT_NAME_PIECE_X = getString(R.string.intent_name_piece_x);
        final String INTENT_NAME_PIECE_Y = getString(R.string.intent_name_piece_y);
        final String INTENT_NAME_MODE = getString(R.string.intent_name_mode);

        // Realm使用開始
        //mRealm = Realm.getDefaultInstance();

        // 起動時にDB消去
        // http://y-yagi.tumblr.com/post/123236341530/realm-for-android-%E3%81%A7db%E3%82%92%E5%89%8A%E9%99%A4%E3%81%99%E3%82%8B
        // 180310: MyActivity onCreateにて、Builder(this)が使えず
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfig);
        mRealm = Realm.getInstance(realmConfig);

        // debug 格納済データのログ出力
        RealmResults<PlayInfo> results = mRealm.where(PlayInfo.class).findAll();
        for (PlayInfo result:
                results) {
            Log.d("ReadFromRealm",result.toString());
        }

        // Realmへ格納するクラスを準備
        // mPlayInfoは1レコードしか持てないので主キー固定
        //mPlayInfo = mRealm.createObject(PlayInfo.class,0);
        mPlayInfo = new PlayInfo();
        mPlayInfo.id = 0; // 主キー

        // アクティビティの引数チェック
        mIntent = getIntent();
        int pieceX = mIntent.getIntExtra(INTENT_NAME_PIECE_X, 4);
        int pieceY = mIntent.getIntExtra(INTENT_NAME_PIECE_Y, mPlayInfo.pieceX);
        // 指定可能な最低値以上へ補正
        mPlayInfo.pieceX = Math.max(pieceX, 3);
        mPlayInfo.pieceY = Math.max(pieceY, 3);
        
        mPlayInfo.mode = mIntent.getIntExtra(INTENT_NAME_MODE, -1);
        //TODO: modeが0未満のとき中断(正しく受け渡せていない)

        // Viewの設定(主にカスタムフォント)
        AssetManager asset = getAssets();
        String fontName = getString(R.string.custom_font_name);

        // スコア
        textScore = (TextView) findViewById(R.id.textScore);
        textScore.setTypeface(Typeface.createFromAsset(asset, fontName));

        // 説明文
        textExplain = (TextView) findViewById(R.id.textExplain);
        textExplain.setTypeface(Typeface.createFromAsset(asset, fontName));

        // SurfaceViewの初期設定
        mSurfaceView = (TranslationSurfaceView) findViewById(R.id.surfaceView);

        // 次へボタンの初期設定
        mBtnNextStage = (Button) findViewById(R.id.btnNextStage);
        mBtnNextStage.setTypeface(Typeface.createFromAsset(asset, fontName));

        // 次へまたは終了ボタン。flagFinalStageで制御
        mBtnNextStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flagFinalStage == false) {
                    // 次のステージを開始
                    loadNewStage();
                    // ボタン自身は再び非表示にする
                    mBtnNextStage.setVisibility(View.INVISIBLE);
                } else {
                    // タイトルへ戻る
                    setResult(RESULT_OK, mIntent);
                    finish();
                }
            }
        });

        // 再開時は設定が異なる？
        // 初期表示
        updateScore(0,false);

//        mHolder = mSurfaceView.getHolder();
//        // コールバック設定
//        mHolder.addCallback(this);
//        // 透過
//        mHolder.setFormat(PixelFormat.TRANSLUCENT);
//        // 一番手前に表示
//        mSurfaceView.setZOrderOnTop(true);

        // ホサカさんが、ThreadをValueAnimatorへ置き換えた
        //mAnimator = ValueAnimator.ofFloat(0f,1f);
        // onCreateに移動したらアプリ再表示で落ちなくなった
        mAnimator = mSurfaceView.getmAnimator();

        // タッチイベントのインスタンスを生成
        mGestureDetector = new GestureDetector(this, mOnGestureListener);

        // Mainが重いので、定義内容の一部をThread化
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

                for (int i = 0; i < mPlayInfo.pieceX; i++) {

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

                    // 縦長LinearLayoutへ追加
                    llSlideArea.addView(llRow);

                    for (int j = 0; j < mPlayInfo.pieceY; j++) {
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
                        //iv.setTag("ImageView-" + String.valueOf(i*mPlayInfo.pieceY+j)); // 多分、1次元配列上の添え字
                        iv.setTag(String.valueOf(i * mPlayInfo.pieceY + j)); // mImagePiece<> 1次元配列上の添え字

                        // 横長LinearLayoutへ追加
                        llRow.addView(iv);

                        // 全CustomViewの1次元リストに追加
                        mImagePieces.add(iv);
                        //Log.d("MainActivity", "mImagePieces id:" + i * mPlayInfo.pieceY + j + " added");

                    } //for j

                } //for i

            }

        })).start();    // Thread末尾＆開始

        // ここより後にonCreateの定義を書かないこと

    }

    // onCreate直後の初回処理用
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (flagSetBitmap) {
            return;
        }

        //TODO 中断データがあれば再開する


        // 次のステージを開始(初回)
        loadNewStage();

        // 再実行しない
        flagSetBitmap = true;
    }

    private class PatternParts {
        int mPattern = SELECT_NONE;
        int mPart = SELECT_NONE;

        PatternParts() {
            this(SELECT_NONE, SELECT_NONE);
        }

        PatternParts(int mPattern, int mPart) {
            setPatternPart(mPattern, mPart);
        }

        void setPattern(int mPattern) {
            this.mPattern = mPattern;
        }

        void setPart(int mPart) {
            this.mPart = mPart;
        }

        void setPatternPart(int mPattern, int mPart) {
            setPattern(mPattern);
            setPart(mPart);
        }

        int getPattern() {
            return mPattern;
        }

        int getPart() {
            return mPart;
        }
    }

    // タッチイベントにジェスチャー受付を定義
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        String action = "";

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                action = "Touch Down";
                break;
            case MotionEvent.ACTION_MOVE:
                action = "Touch Move";
                break;
            case MotionEvent.ACTION_UP:

                // アニメーション中でなければ何もしない
                if (flagAnimeReady == false) {
                    break;
                }
                //Log.d("TapUp","mAnimeSrcIndex="+mAnimeSrcIndex+",mAnimeDestIndex="+mAnimeDestIndex);

                // 移動先がない場合は何もしない（何もできない）
                if (mAnimeDestIndex <= SELECT_NONE) {
                    flagAnimeReady = false;
                    break;
                }

                final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
                // 模様の成立チェック
                int resId = destImageView.getResId();
                // 連続アニメーション可能にしたため、うまくいかないときがあるのを回避
                // 厳密にはテストできていない
                if (resId <= SELECT_NONE) {
                    flagAnimeReady = false;
                    break;
                }

                // 最低2箇所チェックする必要がある
                boolean flagMatch1 = false;
                boolean flagMatch2 = false;

                // フリック方向に基づき、チェック対象を決定
                switch (mAnimeDirection) {
                    case DIRECTION_TOP:
                        // 左上、右上
                        flagMatch1 = checkMatch(resId,PART_UL);
                        flagMatch2 = checkMatch(resId,PART_UR);

                        // 消去
                        if (flagMatch1) {
                            vanishMatch(PART_UL);
                        }
                        if (flagMatch2) {
                            vanishMatch(PART_UR);
                        }
                        break;
                    case DIRECTION_LEFT:
                        // 左上、左下
                        flagMatch1 = checkMatch(resId,PART_UL);
                        flagMatch2 = checkMatch(resId,PART_LL);

                        // 消去
                        if (flagMatch1) {
                            vanishMatch(PART_UL);
                        }
                        if (flagMatch2) {
                            vanishMatch(PART_LL);
                        }

                        break;
                    case DIRECTION_RIGHT:
                        // 右上、右下
                        flagMatch1 = checkMatch(resId,PART_UR);
                        flagMatch2 = checkMatch(resId,PART_LR);

                        // 消去
                        if (flagMatch1) {
                            vanishMatch(PART_UR);
                        }
                        if (flagMatch2) {
                            vanishMatch(PART_LR);
                        }
                        break;
                    case DIRECTION_BOTTOM:
                        // 左下、右下
                        flagMatch1 = checkMatch(resId,PART_LL);
                        flagMatch2 = checkMatch(resId,PART_LR);

                        // 消去
                        if (flagMatch1) {
                            vanishMatch(PART_LL);
                        }
                        if (flagMatch2) {
                            vanishMatch(PART_LR);
                        }
                        break;
                }

                // 模様を消したことによる加算
                if (flagMatch1 && flagMatch2) {
                    // 2個同時消し
                    mPlayInfo.vanishMultiCount += 1;
                    mPlayInfo.vanishCount += 2;
                    updateScore(SCORE_VANISH_1 * 2 + SCORE_VANISH_2, false);
                } else if (flagMatch1 || flagMatch2 ) {
                    // 1個だけ
                    mPlayInfo.vanishCount += 1;
                    updateScore(SCORE_VANISH_1, false);
                }

                //Log.d("check","mAnimeDirection="+mAnimeDirection
                //        +",flagMatch1="+flagMatch1+",flagMatch2="+flagMatch2);

                // 模様のマッチが成立したらステージクリア判定
                if (flagMatch1 || flagMatch2) {
                    boolean flagClear = checkStageClear();
                    // ステージクリアしたら、次へボタンを表示
                    if (flagClear) {
                        updateScore(SCORE_CLEAR + (mPlayInfo.stageNumber-1) * 2, false);
                        mBtnNextStage.setVisibility(View.VISIBLE);
                    }
                }

                flagAnimeReady = false;
                // 他のメンバ変数のクリア
                //mAnimeSrcIndex = SELECT_NONE;
                //mAnimeDestIndex = SELECT_NONE;

                action = "Touch Up";
                //Log.d("Touch", action + " x=" + x + ", y=" + y);
                break;

            case MotionEvent.ACTION_CANCEL:
                action = "Touch Cancel";
                break;
        }
        //Log.d("Touch", action + " x=" + x + ", y=" + y);
        //return super.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    // タッチイベントのリスナー
    // fling (過去形)flung (過去分詞)flung
    private final GestureDetector.SimpleOnGestureListener mOnGestureListener
             = new GestureDetector.SimpleOnGestureListener() {

        // タップ位置＝アニメーション開始位置のCustomViewのIdを記録
        @Override
        public boolean onDown(MotionEvent event) {
            int flungViewIndex = SELECT_NONE;
            try {
                // どのPieceのView上かチェック
                flungViewIndex = getViewIndex(event.getX(), event.getY());
                // 有効な値でなければ処理しない
                if (flungViewIndex <= SELECT_NONE || mImagePieces.size() <= flungViewIndex) {
                    return super.onDown(event);
                }

                //　非表示状態のCustomViewだったら処理しない
                if (mImagePieces.get(flungViewIndex).getVisibility() != View.VISIBLE) {
                    return super.onDown(event);
                }
            } catch (Exception e) {
            }

            // このCustomViewの中心位置の座標を記録
            CustomView cv = mImagePieces.get(flungViewIndex);
            int[] lo = new int[2];
            cv.getLocationInWindow(lo);
            int cvLeft = lo[0];
            int cvTop = lo[1];
            mAnimeSrcCenterX = cvLeft + cv.getWidth() / 2;
            mAnimeSrcCenterY = cvTop + cv.getHeight() / 2;

            mAnimeSrcIndex = flungViewIndex;
            flagAnimeReady = true;
            return super.onDown(event);
        }

        // https://qiita.com/shinido/items/65399846a5e9eba1aa5e
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            final int SWIPE_MIN_DISTANCE = 50;          // 最小移動距離
//            final int SWIPE_THRESHOLD_VELOCITY = 200;   // 最小速度
            final float SLOPE_RATE = 1.1f;              // 斜めと判定されないための比率

            // 移動方向
            int directionX = DIRECTION_NONE;
            int directionY = DIRECTION_NONE;
            int direction  = DIRECTION_NONE;

            //Log.d("onScroll","from:("+event1.getX()+","+event1.getY()+
            //        ") to:("+event2.getX()+","+event2.getY()+")");
            // イベントが頻繁に実行されるため、ログは最小限にした方が良い

            // アニメーション管理中以外は何もしない
            if (flagAnimeReady == false) {
                return super.onScroll(event1, event2, distanceX, distanceY);
            }
            // アニメーション中は何もしない
            if (mAnimator.isRunning()) {
                return super.onScroll(event1, event2, distanceX, distanceY);
            }

            // 移動距離・スピードを出力
//            float distance_y = event1.getY() - event2.getY();
//            float distance_x = event2.getX() - event1.getX();

            try {

                // 前回アニメ終了位置(最初の開始位置含む)からの移動距離
                float distance_x = event2.getX() - mAnimeSrcCenterX;
                float distance_y = mAnimeSrcCenterY - event2.getY();
                String strX = null;
                String strY = null;

                // X・Y軸それぞれの方向性の有無を確認

                // 開始位置から終了位置の移動距離がSWIPE_MIN_DISTANCEより大きい
                if (distance_x < -SWIPE_MIN_DISTANCE) {
                    strX = "左";
                    directionX = DIRECTION_LEFT;

                // 終了位置から開始位置の移動距離がSWIPE_MIN_DISTANCEより大きい
                } else if (distance_x > SWIPE_MIN_DISTANCE) {
                    strX = "右";
                    directionX = DIRECTION_RIGHT;

                }

                // 終了位置から開始位置の移動距離がSWIPE_MIN_DISTANCEより大きい
                if (distance_y > SWIPE_MIN_DISTANCE) {
                    strY = "上";
                    directionY = DIRECTION_TOP;

                    // 開始位置から終了位置の移動距離がSWIPE_MIN_DISTANCEより大きい
                } else if (distance_y < -SWIPE_MIN_DISTANCE) {
                    strY = "下";
                    directionY = DIRECTION_BOTTOM;

                }

                // 方向の決定
                // event1:移動開始 event2:移動終了
                // X軸は、右が大きい。Y軸は下が大きい。
                if (Math.abs(distance_x) > Math.abs(distance_y) * SLOPE_RATE) {
                    // X方向が大きい
                    //Log.d("onScroll", "CustomView["+mAnimeSrcIndex + "]が" + strX + "方向");
                    direction = directionX;

                } else if (Math.abs(distance_x) * SLOPE_RATE < Math.abs(distance_y)) {
                    // Y方向が大きい
                    //Log.d("onScroll","CustomView["+mAnimeSrcIndex + "]が" + strY + "方向");
                    direction = directionY;

                } else {
                    // ほぼ等しいは無視扱い
                    //Log.d("onScroll", "CustomView["+mAnimeSrcIndex + "]が、ほぼ斜め" + strX + strY + "方向");
                    direction = DIRECTION_NONE;
                }

            } catch (Exception e) {
            }
            if (direction == DIRECTION_NONE) {
                // まだアニメーションしない
                return super.onScroll(event1, event2, distanceX, distanceY);
            }

            // 移動先のIdの取得を試みる
            int destViewIndex = getDestViewIndex(mAnimeSrcIndex, direction, true);
            // 移動不可の場合、SELECT_NONEが返るのでアニメーションしない
            if (destViewIndex <= SELECT_NONE) {
//                flagAnimeReady = false;
                return super.onScroll(event1, event2, distanceX, distanceY);
            }

            // このCustomViewの中心位置の座標を記録
            CustomView cv = mImagePieces.get(destViewIndex);
            int[] lo = new int[2];
            cv.getLocationInWindow(lo);
            int cvLeft = lo[0];
            int cvTop = lo[1];
            // direction方向へ十分移動したかの境界(180211: 3分の1とする)
            float borderRatio = 0.25F;
            int AnimeDestBorderLeft = cvLeft + (int)(cv.getWidth() * borderRatio);
            int AnimeDestBorderTop = cvTop + (int)(cv.getHeight() * borderRatio);
            int AnimeDestBorderRight = cvLeft + (int)(cv.getWidth() * (1.0F - borderRatio));
            int AnimeDestBorderBottom = cvTop + (int)(cv.getHeight() * (1.0F - borderRatio));

            //Log.d("onScroll","Current("+event2.getX()+","+event2.getY()+")"+
            //    "AnimeDestBorder L,T,R,B=("+AnimeDestBorderLeft+","+AnimeDestBorderTop+
            //        ","+AnimeDestBorderRight+","+AnimeDestBorderBottom+")");

            // 通り過ぎていれば移動したとみなす
            boolean flagMoveValid = false;
            switch(direction) {
                case DIRECTION_TOP:
                    if (event2.getY() < AnimeDestBorderBottom) {
                        flagMoveValid = true;
                    }
                    break;
                case DIRECTION_LEFT:
                    if (event2.getX() < AnimeDestBorderRight) {
                        flagMoveValid = true;
                    }
                    break;
                case DIRECTION_RIGHT:
                    if (event2.getX() > AnimeDestBorderLeft) {
                        flagMoveValid = true;
                    }
                    break;
                case DIRECTION_BOTTOM:
                    if (event2.getY() > AnimeDestBorderTop) {
                        flagMoveValid = true;
                    }
                    break;
            }
            if (!flagMoveValid) {
                return super.onScroll(event1, event2, distanceX, distanceY);
            }
            // 移動先のCustomViewの添え字、中心座標
            mAnimeDestIndex = destViewIndex;
            mAnimeDestCenterX = cvLeft + cv.getWidth() / 2;
            mAnimeDestCenterY = cvTop + cv.getHeight() / 2;
            // 1マス移動してまだ移動中。スコアを減らす、移動数を増やす
            updateScore(-1, true);
            //Log.d("score","move next. score update");

            //Log.d("onScroll", "src: "+mAnimeSrcIndex+" dest: "+mAnimeDestIndex);

            // アニメーション中は次のアニメーションは禁止
            if (mAnimator.isRunning()) {
                return super.onScroll(event1, event2, distanceX, distanceY);
            }

            // 移動可能なのでアニメーション準備
            mAnimeDirection = direction;
//            mAnimeSrcIndex = flungViewIndex;
//            mAnimeDestIndex = destViewIndex;

            // アニメーション時間を設定
            mAnimator.setDuration(ANIME_DURATION);

            // 移動元の画像の取得
            CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);
            mBitmapId = srcImageView.getResId();
            if (mBitmapId <= SELECT_NONE) {
                return false;
            }
            mBitmap = mBitmapList.get(mBitmapId);

            // 描画開始位置
            int srcX = srcImageView.getLeft();
            // Y座標は親View(横長LinearLayout)から取得
            // http://ichitcltk.hustle.ne.jp/gudon2/index.php?pageType=file&id=Android059_ViewTree
            int srcY = ((View) srcImageView.getParent()).getTop();
            // Log.d("location", "imageView X=" + srcX + ",Y=" + srcY);

            // CustomViewの各パネルのマージンの分だけ表示位置を補正
            // 縦が不要なのは、余白なしの上位のLinearLayoutの座標を使用しているため
            //srcX += PIECE_MARGIN;                   // マージン補正←不要
            srcY += PIECE_MARGIN;                   // マージン補正

            // 移動先座標。switch caseで制御
            int destX = 0;
            int destY = 0;

            // X方向・Y方向の移動先
            switch (mAnimeDirection) {
                case DIRECTION_TOP:
                    destX = srcX;
                    destY = srcY - ((View) srcImageView.getParent()).getHeight();
                    break;

                case DIRECTION_LEFT:
                    destX = srcX - srcImageView.getWidth();
                    destX -= PIECE_MARGIN * 2;      // マージン補正
                    destY = srcY;
                    break;

                case DIRECTION_RIGHT:
                    destX = srcX + srcImageView.getWidth();
                    destX += PIECE_MARGIN * 2;      // マージン補正
                    destY = srcY;
                    break;

                case DIRECTION_BOTTOM:
                    destX = srcX;
                    destY = srcY + ((View) srcImageView.getParent()).getHeight();
                    break;
            }

            // アニメーション情報を設定
            mSurfaceView.setAnimationInfo(mBitmap,srcX,srcY,destX,destY);

            // アニメーション開始前・終了後の処理をコールバックに追加
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    // 移動元ImageViewの画像を非表示にする
                    final CustomView srcImageView = mImagePieces.get(mAnimeSrcIndex);
                    srcImageView.setVisibility(View.INVISIBLE);
                    srcImageView.setResId(SELECT_NONE);

                    // 移動先ImageViewに画像を書き込む
                    final CustomView destImageView = mImagePieces.get(mAnimeDestIndex);
                    destImageView.setImageBitmap(mBitmap);
                    destImageView.setResId(mBitmapId);

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
                    destImageView.setVisibility(View.VISIBLE);
                    //// 移動終了。スコアを減らす、移動数を増やす
                    //updateScore(-1, true);
                    //Log.d("score","move end. score update");

                    super.onAnimationEnd(animation);
                }
            });
            mAnimator.start();

            // 移動先が次の移動元になる
            //Log.d("onScroll","Dest["+mAnimeDestIndex+"] => Src["+mAnimeSrcIndex+"]");
            mAnimeSrcIndex = mAnimeDestIndex;
            mAnimeSrcCenterX = mAnimeDestCenterX;
            mAnimeSrcCenterY = mAnimeDestCenterY;

            return super.onScroll(event1, event2, distanceX, distanceY);
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
                    parts = new int[]{PART_UR,PART_LL,PART_UL};
                    break;
                default:
                    return;
            }
            this.basePart = basePart;
        }

        // 有効な要素数を返す
        public int getSize() {
            if (basePart > SELECT_NONE) {
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

    // ステージをクリアしたかチェック
    private boolean checkStageClear() {
        //boolean flagClear = true; // クリアしている(可能性がある)=true、いない=false(今のところ不要)
        // 2次元配列のforeach
        // https://teratail.com/questions/39814
        for (int[] imgRes: aryImgRes) {     // forjループにするよりは添え字表記が減っている
            for (int i = 0; i < 4; i++) {   // 当面は4つマッチとする。5つ目を使わないためforeach不可
                // まだマッチさせていない箇所があれば、クリアしていない
                if (imgRes[i] > SELECT_NONE) {
                    Log.d("checkStageClear",Arrays.deepToString(aryImgRes));
                    return false;
                }
            }
        }
        Log.d("checkStageClear","Stage: "+mPlayInfo.stageNumber+" Clear!");
        return true;
    }

    // 模様の成立をチェックする
    private boolean checkMatch(int resId, int part) {
        int code = aryImgRes[resId][part];
        // 配列の宣言と初期化を別々に行う
        // http://blog.goo.ne.jp/xypenguin/e/e1cfcc0b1a8c3acdbe023bbef8944dac
        //int[] directions = null;    // = new int[3];
        //int[] positions = null;     // = new int[3];

        // チェック対象をクラスで生成
        Positions positions = new Positions(part);
        // ポジションが取得できなかったら不成立
        if (positions.getSize() <= SELECT_NONE) {
            return false;
        }

        // 正否判定用
        boolean flagMatch = true; // マッチしている(可能性がある)=true、いない=false

        // 基準位置のコードを取得
        int codeGroup = code / 4; // 当面は4つマッチとする
        //Log.d("check","resId"+resId+",code="+code);

        // SELECT_NONEでなければ、周辺のコードを取得
        if (code > SELECT_NONE) {
            for (int i = 0; i < positions.getSize(); i++) {
                int targetCode = getAroundCode(mAnimeDestIndex,
                        positions.getDirection(i), positions.getPart(i));
                //Log.d("check","targetCode="+targetCode);
                // マッチしないまたはSELECT_NONEあれば判定終了
                if ( targetCode <= SELECT_NONE || (targetCode / 4) != codeGroup) {
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
    private int getAroundCode(int srcViewIndex,int direction,int part) {
        int targetViewIndex;
        int targetResId;
        int targetCode;
        // srcViewIndexのCustomViewから見て、direction方向にあるCustomViewのIdを取得
        targetViewIndex = getDestViewIndex(srcViewIndex,direction,false);
        if (targetViewIndex <= SELECT_NONE) {
            //Log.d("getAroundCode","targetViewIndex=SELECT_NONE");
            return SELECT_NONE;
        }
        // CustomViewの保持する画像リソースIdを取得
        targetResId = mImagePieces.get(targetViewIndex).getResId();
        if (targetResId <= SELECT_NONE) {
            //Log.d("getAroundCode","targetResId=SELECT_NONE");
            return SELECT_NONE;
        }
        // 画像リソースの部位のコードを取得
        targetCode = aryImgRes[targetResId][part];
        return targetCode;
    }

    // 模様の成立した4パネルの該当部位の画像コード及び画像を消去する
    private boolean vanishMatch(int part) {
        // チェック対象をクラスで生成
        Positions positions = new Positions(part);
        // ポジションが取得できなかったら不成立
        if (positions.getSize() <= SELECT_NONE) {
            return false;
        }

        // 各部位の画像消去
        for (int i = 0; i < positions.getSize(); i++) {
            updateImagePart(mAnimeDestIndex,
                    positions.getDirection(i), positions.getPart(i), SELECT_NONE2);
        }
        updateImagePart(mAnimeDestIndex, DIRECTION_NONE, part, SELECT_NONE2);
        return true;
    }

    // 指定部位の画像コードをtargetCodeで更新し、
    // CustomViewの画像を更新する
    //課題: 消す場合は、SELECT_NONEでないコードと画像を設定するべき？
    @SuppressLint("ResourceAsColor")
    private int updateImagePart(int srcViewIndex, int direction, int part, int targetCode) {
        int targetViewIndex;
        int targetResId;
        //int targetCode;
        // srcViewIndexのCustomViewから見て、direction方向にあるCustomViewのIdを取得
        if (direction == DIRECTION_NONE) {
            // 方向なしは、引数自身
            targetViewIndex = srcViewIndex;
        } else {
            // 指定された方向から、Idを探す
            targetViewIndex = getDestViewIndex(srcViewIndex, direction, false);
        }
        // 見つからなかったら終了
        if (targetViewIndex <= SELECT_NONE) {
            //Log.d("getAroundCode","targetViewIndex=SELECT_NONE");
            return SELECT_NONE;
        }
        // CustomViewの保持する画像リソースIdを取得
        targetResId = mImagePieces.get(targetViewIndex).getResId();
        if (targetResId <= SELECT_NONE) {
            //Log.d("getAroundCode","targetResId=SELECT_NONE");
            return SELECT_NONE;
        }
        // 画像リソースのpart位置のコードを書き換える
        aryImgRes[targetResId][part] = targetCode;

        Log.d("vanish","targetResId="+ targetResId);

        // 画像初期設定をコピーして加工（ここから）

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
        Canvas canvas = new Canvas(mBitmapList.get(targetResId));

        // コードをリソースIdへ変換
        int resId = c2r.getResId(aryImgRes[targetResId][part]);
        // Bitmapをリソースから読み込む
        Bitmap bitmapWork1;
        Bitmap bitmapWork2;
        if (aryImgRes[targetResId][part] > SELECT_NONE) {
            // 画像あり
            bitmapWork1 = BitmapFactory.decodeResource(resources, resId);
            // サイズ補正（AccBall参照）
            bitmapWork2 = Bitmap.createScaledBitmap(bitmapWork1,
                    viewWidthHalf, viewHeightHalf, false);
        } else {
            // 消えた後の色だけの画像を生成。空のときと異なる色にしている
            bitmapWork2 = Bitmap.createBitmap(viewWidthHalf, viewHeightHalf, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = new Canvas(bitmapWork2);
            // 定義した色を使用
            // http://furudate.hatenablog.com/entry/2013/06/19/010953
            canvas2.drawColor(resources.getColor(mVanishColor));
        }
        // 用意した画像を指定の位置へ追加
        canvas.drawBitmap(bitmapWork2,
                bitmapLeft, bitmapTop, (Paint) null);

        // 該当CustomViewへ画像を設置
        CustomView destImageView = mImagePieces.get(targetViewIndex);
        destImageView.setImageBitmap(mBitmapList.get(targetResId));

        // 画像初期設定をコピーして加工（ここまで）

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
    }

    // アクティビティ終了時
    @Override
    protected void onDestroy() {
        //ここは不要？
        // // TitleActivityに返す。返すパラメータを変えるかも
        //setResult(RESULT_CANCELED, mIntent);
        //finish();

        // Realm終了
        mRealm.close();
        super.onDestroy();
    }

    // ダイアログからのコールバック
    // http://www.ipentec.com/document/android-custom-dialog-using-dialogfragment-return-value
    public void onReturnValue(int value) {
        if (value == RETURN_YES) {
            //TODO アプリ中断処理
            savePlayInfo();

            // TitleActivityに返す。返すパラメータを変えるかも
            setResult(RESULT_CANCELED, mIntent);
            finish();
        }
    }

    // ステージの状態をRealmへ保存する
    public void savePlayInfo() {
        // 保存する中断データ(ステージの状態)は1つだけとする
        // 既存の全レコードを取得し、削除する
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // 全レコード取得、リスト化
                RealmResults<PlayInfo> playInfos
                        = realm.where(PlayInfo.class).findAll();

                // 削除
                for (PlayInfo playInfo:
                        playInfos) {
                    // 1レコード削除
                    playInfo.deleteFromRealm();
                }
            }
        });

        // 各パネル上の模様(resId)の抽出
        ArrayList<Integer> resIdList = new ArrayList<>();
        int panelMax = mImagePieces.size();
        for (int i = 0; i < panelMax; i++) {
            int resId = mImagePieces.get(i).getResId();
            resIdList.add(resId);
        }

        // シリアライザ
        Gson gson = new Gson();
        // Integer型 ArrayList
        mPlayInfo.jsonPanelResIdList = gson.toJson(resIdList);
        Log.d("gson-java", resIdList.toString());
        Log.d("Gson-JSON", "mPlayInfo.jsonPanelResIdList="+mPlayInfo.jsonPanelResIdList);

        // 参考(ArrayListに戻す場合)
        // https://qiita.com/hisurga/items/a02436e03ea2aba6c6db

        // int型 2次元配列
        mPlayInfo.jsonAryImgRes = gson.toJson(aryImgRes);
        Log.d("gson-java", Arrays.deepToString(aryImgRes));
        Log.d("Gson-JSON", "mPlayInfo.jsonAryImgRes="+mPlayInfo.jsonAryImgRes);

        mPlayInfo.date = new Date();

        // Realmへ登録(ScheduleBookとは異なる)
        // https://qiita.com/roana0229/items/e641da94ab4ebad46b4c
        mRealm.beginTransaction();
        mRealm.copyToRealm(mPlayInfo);
        mRealm.commitTransaction();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 戻るボタンを押したとき
        // http://www.ore-memo.com/989.html
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // 終了確認ダイアログ
            DialogFragment newFragment = new SuspendDialog();
            newFragment.show(getFragmentManager(), "suspend");
        }
        return super.onKeyDown(keyCode, event);
    }

    // スコア更新
    @SuppressLint("DefaultLocale")
    void updateScore(int score, boolean flagMove) {
        if (flagMove) {
            mPlayInfo.moveCount++;
            // さばいばるの場合は一定回数移動したら部位をランダム追加
            if (mPlayInfo.mode == INTENT_MODE_SURVIVAL && mPlayInfo.moveCount % 6 == 0) { // % 5
                int patternNumber = Math.min(c2r.getPatternNumber(), mPlayInfo.pieceX + 1);
                addSurvivalParts(patternNumber, 3); //mPlayInfo.pieceX + 1
            }
        }
        mPlayInfo.score += score;
        // maroon
        // darkgreen
        // midnightblue + 少々
        textScore.setText(Html.fromHtml(String.format(
                "<font color=\"#800000\"><big>%d</big>%s</font><br/>"+
                "<font color=\"#006400\"><big>%d</big>%s</font><br/>"+
                "<font color=\"#303090\"><big>%d</big>%s</font>",
                mPlayInfo.moveCount, getString(R.string.text_move),
                mPlayInfo.vanishCount, getString(R.string.text_vanish),
                mPlayInfo.score, getString(R.string.text_score))));
    }

    // フリックの開始位置にあるCustomViewのIdを取得
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

            // 判定を厳しくする(余白の内側のみにする)→しなくていい
            // ※余白は、Viewのpaddingプロパティにするのもあり
            //ivLeft   += PIECE_MARGIN;
            //ivTop    += PIECE_MARGIN;
            //ivRight  -= PIECE_MARGIN;
            //ivBottom -= PIECE_MARGIN;

            // マイナス(矛盾)を補正しないのは、次のif文が成立しないため

            // 引数の座標がこのView内であるか判定
            if (ivLeft < flingX && flingX < ivRight
                    && ivTop < flingY && flingY < ivBottom ) {
                //Log.d("onScroll", "In ImageView-" + i);
                // Viewが見つかったのでループ中断
                index = i;
                break;
            } //if

        } //for i
        return index;
    }

    // フリックによる移動先のCustomViewのIdを取得(無いかもしれない) (flagMove=true)
    // あるいは、単に指定方向のCustomViewのIdを取得(無いかもしれない) (flagMove=false)
    private int getDestViewIndex(int srcIndex,int direction, boolean flagMove) {

        int destIndex = SELECT_NONE;

        //////////////////////////////
        // フリック方向別に算出
        switch(direction) {
            case DIRECTION_TOP:
                // 上方向へ移動
                destIndex = srcIndex - mPlayInfo.pieceX;
                if (destIndex < 0) {
                    destIndex = SELECT_NONE;
                }
                break;

            case DIRECTION_LEFT:
                // 左方向へ移動
                if (srcIndex % mPlayInfo.pieceX == 0) {
                    destIndex = SELECT_NONE;
                } else {
                    destIndex = srcIndex - 1;
                }
                break;

            case DIRECTION_RIGHT:
                // 右方向へ移動
                if ((srcIndex + 1) % mPlayInfo.pieceX == 0) {
                    destIndex = SELECT_NONE;
                } else {
                    destIndex = srcIndex + 1;
                }
                break;

            case DIRECTION_BOTTOM:
                // 下方向へ移動
                destIndex = srcIndex + mPlayInfo.pieceX;
                // 要素数以上なので、mImagePieces.size() でも可
                if (destIndex >= mPlayInfo.pieceX * mPlayInfo.pieceY ) {
                    destIndex = SELECT_NONE;
                }
                break;

            case DIRECTION_TOP_LEFT:
                // 上方向へ移動
                destIndex = srcIndex - mPlayInfo.pieceX;
                if (destIndex < 0) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、左方向へ移動
                    if (destIndex % mPlayInfo.pieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex - 1;
                    }
                }
                break;

            case DIRECTION_TOP_RIGHT:
                // 上方向へ移動
                destIndex = srcIndex - mPlayInfo.pieceX;
                if (destIndex < 0) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、右方向へ移動
                    if ((destIndex + 1) % mPlayInfo.pieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex + 1;
                    }
                }
                break;

            case DIRECTION_BOTTOM_LEFT:
                // 下方向へ移動
                destIndex = srcIndex + mPlayInfo.pieceX;
                // 要素数以上なので、mImagePieces.size() でも可
                if (destIndex >= mPlayInfo.pieceX * mPlayInfo.pieceY ) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、左方向へ移動
                    if (destIndex % mPlayInfo.pieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex - 1;
                    }
                }
                break;

            case DIRECTION_BOTTOM_RIGHT:
                // 下方向へ移動
                destIndex = srcIndex + mPlayInfo.pieceX;
                // 要素数以上なので、mImagePieces.size() でも可
                if (destIndex >= mPlayInfo.pieceX * mPlayInfo.pieceY ) {
                    destIndex = SELECT_NONE;
                } else {
                    // さらに、右方向へ移動
                    if ((destIndex + 1) % mPlayInfo.pieceX == 0) {
                        destIndex = SELECT_NONE;
                    } else {
                        destIndex = destIndex + 1;
                    }
                }
                break;

        }
        // 移動不可確定、または、移動先を返す
        if (destIndex <= SELECT_NONE || flagMove == false) {
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

    // 次のステージを開始する
    void loadNewStage() {
        // 各パネルの画像情報のクリア
        for (CustomView cv: mImagePieces) {
            cv.setResId(SELECT_NONE);
            cv.setVisibility(View.INVISIBLE);
        }
        // ビットマップリストのクリア
        mBitmapList.clear();

        // ステージ番号をインクリメント
        mPlayInfo.stageNumber++;
        //        // エンドレス
        //        if mPlayInfo.stageNumber >= 8) {
        //            mPlayInfo.stageNumber = 7;
        //        }

        //// test用。ステージ番号を固定するときに解除
        //mPlayInfo.stageNumber = 8;

        // http://blog.goo.ne.jp/xypenguin/e/e1cfcc0b1a8c3acdbe023bbef8944dac
        // コード値の組と、初期配置先CustomViewの添え字を更新
        final int partNumber = 4;
        int patternNumber = 0;
        int panelNumber = 0;
        switch (mPlayInfo.mode) {
            case INTENT_MODE_TRIAL:
                createTrialStage();
                break;

            case INTENT_MODE_EASY:
                // かんたん
                // ランダムステージ

//                // アイコン用 3x3 で 2模様
//                aryImgRes = new int[][]{
//                        {SELECT_NONE, SELECT_NONE, SELECT_NONE,           7, 1},
//                        {          4, SELECT_NONE, SELECT_NONE,           3, 3},
//                        {SELECT_NONE,           5,           2, SELECT_NONE, 5},
//                        {SELECT_NONE,           1, SELECT_NONE, SELECT_NONE, 6},
//                        {          0, SELECT_NONE,           6, SELECT_NONE, 7},
//                };
//                break;

//                // 乱数(1～Nまで)
//                // http://adash-android.jp.net/android%E3%81%A7%E4%B9%B1%E6%95%B0%E3%82%92%E5%8F%96%E5%BE%97/
//                Random r = new Random();
//                //panelNumber = r.nextInt(mPlayInfo.pieceX * mPlayInfo.pieceY - 2) + 1;
//                panelNumber = r.nextInt(mPlayInfo.pieceX * mPlayInfo.pieceY - 4) + 4;
//                patternNumber = r.nextInt(Math.min(c2r.getPatternNumber(),panelNumber)) + 1;

                //　ステージ進行に合わせて上昇する乱数の最大値
                // http://adash-android.jp.net/android%E3%81%A7%E4%B9%B1%E6%95%B0%E3%82%92%E5%8F%96%E5%BE%97/
                Random r = new Random();

                // ベースの4に加算する最大パネル数
                // 3x3: stage/8 + 1(=3/2)
                // 4x4: stage/8 + 2(=4/2)
                // 5x5: stage/8 + 2(=5/2)
                // 6x6: stage/8 + 3(=6/2)
                // ベースの1に加算する最大パターン数
                // 3x3: stage/6 + 0(=3/2-1)
                // 4x4: stage/6 + 1(=4/2-1)
                // 5x5: stage/6 + 1(=5/2-1)
                // 6x6: stage/6 + 2(=6/2-1)
                int maxAddPanel = (mPlayInfo.stageNumber / 8) + (mPlayInfo.pieceX / 2);
                // 乱数の最大値がmaxAddPanelなら、+1が必要
                maxAddPanel = Math.min(mPlayInfo.pieceX * mPlayInfo.pieceY - 4, maxAddPanel + 1);
                panelNumber = r.nextInt(maxAddPanel) + 4;

                int maxAddPatten = (mPlayInfo.stageNumber / 6) + (mPlayInfo.pieceX / 2 - 1);
                // 乱数の最大値がmaxAddPatternなら、+1が必要
                maxAddPatten = Math.min(c2r.getPatternNumber(), maxAddPatten + 1);
                patternNumber = r.nextInt(maxAddPatten) + 1;

                createEasyStage(patternNumber, panelNumber);
                break;

            case INTENT_MODE_SURVIVAL:
                // さばいばる
                // 1ステージ

                // クリアできそうな模様の種類数
                patternNumber = Math.min(c2r.getPatternNumber(), mPlayInfo.pieceX + 1);

                // 上下左右の辺にを角以外埋めるくらいの個数
                panelNumber = (Math.max(mPlayInfo.pieceX, mPlayInfo.pieceY) - 2) * 5;
                createSurvivalStage(patternNumber, panelNumber);
                break;

            case INTENT_MODE_HARD:
                break;

        }

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
            canvas.drawColor(resources.getColor(mBackgroundColor)); // 背景色を指定

            Bitmap bitmapWork1;
            Bitmap bitmapWork2;
            int resId;
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 2; i++) {
                    int partId = i + j * 2;
                    if (aryImgRes[k][partId] > SELECT_NONE) {
                        // コードをリソースIdへ変換
                        resId = c2r.getResId(aryImgRes[k][partId]);
                        // 180216: ステージ生成時にぬるぽで異常終了があり、以下でdebugした
                        //Log.d("loadNewStage","resId="+resId+",aryImageRes[k="+k+"][BitmapId="+BitmapId+"]="+aryImgRes[k][BitmapId]);

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
            destImageView.setResId(k);
            destImageView.setVisibility(View.VISIBLE);
            //TODO:
            mBitmapList.add(bitmapBase);
        }

    }

    // とらいある
    private void createTrialStage() {
        switch (mPlayInfo.stageNumber) {
            case 1:
                // 1個
                aryImgRes = new int[][]{
                        {SELECT_NONE, SELECT_NONE, SELECT_NONE,           3, 4},
                        {SELECT_NONE, SELECT_NONE,           2, SELECT_NONE, 7},
                        {SELECT_NONE,           1, SELECT_NONE, SELECT_NONE, 8},
                        {          0, SELECT_NONE, SELECT_NONE, SELECT_NONE, 9},
                };
                break;

            case 2:
                // 1個、バラバラ
                aryImgRes = new int[][]{
                        {SELECT_NONE, SELECT_NONE, SELECT_NONE,           3, 0},
                        {SELECT_NONE, SELECT_NONE,           2, SELECT_NONE, 3},
                        {SELECT_NONE,           1, SELECT_NONE, SELECT_NONE,12},
                        {          0, SELECT_NONE, SELECT_NONE, SELECT_NONE,15},
                };
                break;

            case 3:
                // 上下2個、まとめ消し
                aryImgRes = new int[][]{
                        {SELECT_NONE, SELECT_NONE, SELECT_NONE,           7, 0},
                        {SELECT_NONE, SELECT_NONE,           6, SELECT_NONE, 1},
                        {SELECT_NONE,           5, SELECT_NONE,           3, 4},
                        {          4, SELECT_NONE,           2, SELECT_NONE, 7},
                        {SELECT_NONE,           1, SELECT_NONE, SELECT_NONE, 8},
                        {          0, SELECT_NONE, SELECT_NONE, SELECT_NONE, 9},
                };
                break;

            case 4:
                // 左右2個、まとめ消し
                aryImgRes = new int[][]{
                        {SELECT_NONE, SELECT_NONE, SELECT_NONE,           7, 0},
                        {SELECT_NONE, SELECT_NONE,           6,           3, 1},
                        {SELECT_NONE,           5, SELECT_NONE, SELECT_NONE, 4},
                        {SELECT_NONE, SELECT_NONE,           2, SELECT_NONE, 2},
                        {          4,           1, SELECT_NONE, SELECT_NONE,13},
                        {          0, SELECT_NONE, SELECT_NONE, SELECT_NONE, 6},
                };
                break;

            case 5:
                // 左右2個+2個 // ステージ4用
                aryImgRes = new int[][]{
                        {         12, SELECT_NONE, SELECT_NONE,           7, 4},
                        {          8,          13,           6,           3, 5},
                        {SELECT_NONE,           5, SELECT_NONE,          11, 8},
                        {SELECT_NONE,           9,           2, SELECT_NONE, 6},
                        {          4,           1,          10,          15,13},
                        {          0, SELECT_NONE,          14, SELECT_NONE,10},
                };
                break;

            case 6:
                // 同じ模様が複数、を2セット
                aryImgRes = new int[][]{
                        {          0,           1,           2,           3, 0},
                        {          0,           1,           2,           3, 2},
                        {          0,           1,           2,           3, 4},
                        {          0,           1,           2,           3, 5},
                        {          4,           5,           6,           7, 7},
                        {          4,           5,           6,           7,10},
                        {          4,           5,           6,           7,14},
                        {          4,           5,           6,           7,15},
                };
                break;

            case 7:
                // 6個(模様の列挙1)
                aryImgRes = new int[][]{
                        {         12,          17, SELECT_NONE,           7, 0},
                        {          8,          13,           6,           3, 1},
                        {         16,           5,          22,          11, 4},
                        {SELECT_NONE,           9,           2,          23, 2},
                        {          4,           1,          10,          15,13},
                        {          0,          21,          18, SELECT_NONE, 6},
                        {         20, SELECT_NONE,          14,          19, 8},
                };

            case 8:
                // 6個(模様の列挙2)
                aryImgRes = new int[][]{
                        {         44, SELECT_NONE,          42,          27, 1},
                        {         40, SELECT_NONE,          26, SELECT_NONE, 3},
                        {SELECT_NONE,          25,          38,          31, 4},
                        {         24,          45,          30,          43, 6},
                        {         36,          29, SELECT_NONE,          35, 9},
                        {         28,          41,          34, SELECT_NONE,11},
                        {SELECT_NONE,          33,          46,          39,12},
                        {         32,          37, SELECT_NONE,          47,14},
                };

                // 最終ステージのため、クリア時のボタンのキャプションとフラグを変更
                mBtnNextStage.setText(getString(R.string.text_final_stage));
                flagFinalStage = true;

                break;

        }
    }

    // かんたん
    private void createEasyStage(int patternNumber, int panelNumber) {
        //int panelNumber = 6;
        //int patternNumber = 4;
        final int partNumber = 4; // 固定
        // 値の補正
        // パネルは、partNumber(4)以上、全マス数-1以下
        if (panelNumber < partNumber) {
            panelNumber = partNumber;
        } else if (panelNumber >= mPlayInfo.pieceX * mPlayInfo.pieceY) {
            panelNumber = mPlayInfo.pieceX * mPlayInfo.pieceY - 1;
        }
        // 模様は、1以上、patternNumber以下(patternNumberなら全て埋まる)
        if (patternNumber < 1) {
            patternNumber = 1;
        } else if (patternNumber > panelNumber) {
            patternNumber = panelNumber;
        }

        // 全要素を初期化
        aryImgRes = new int[panelNumber][(partNumber+1)];
        for (int[] imgRes: aryImgRes) {
            Arrays.fill(imgRes, SELECT_NONE);
        }

        // パネルをランダムに並べた配列
        int[] panels = c2r.getRandomPermutations(mPlayInfo.pieceX * mPlayInfo.pieceY - 1, panelNumber);
        // 部位をランダムに並べた配列
        int[] parts  = c2r.getRandomPermutations(partNumber, partNumber);

        Log.d("random","pieces[]="+Arrays.toString(panels));
        Log.d("random","pieces[]="+Arrays.toString(parts));

        // 各模様のIdをランダム値にして該当位置の配列要素へ代入
        // 部位毎に処理
        ArrayList<Integer> patterns = c2r.getRandomPermutationsPadding(panelNumber, patternNumber);
        for (int j = 0; j < partNumber; j++) {
            Log.d("random",j+" col pattern="+Arrays.toString(patterns.toArray()));
            for (int i = 0; i < panelNumber; i++) {
                int pattern = patterns.get(i);
                if (pattern > SELECT_NONE) {
                    aryImgRes[i][parts[j]] = pattern * partNumber + parts[j];
                }
            }
            // リストの先頭の要素を末尾へ(1ずらす)→重複
            int pattern = patterns.get(0);  // 先頭の要素を取得
            patterns.remove(0);        // 先頭の要素を削除
            patterns.add(pattern);          // 先頭の要素を末尾へ追加
        }
        // パネル設置位置
        for (int i = 0; i < panelNumber; i++) {
            aryImgRes[i][partNumber] = panels[i];
        }

        // debug 2次元配列の出力
        // https://teratail.com/questions/533
        Log.d("random",Arrays.deepToString(aryImgRes));

    }

    // さばいばる
    private void createSurvivalStage(int patternNumber, int panelNumber) {
        final int partNumber = 4; // 固定
        // 値の補正
        // パネルは、partNumber(4)以上、全マス数-1以下
        if (panelNumber < partNumber) {
            panelNumber = partNumber;
        } else if (panelNumber >= mPlayInfo.pieceX * mPlayInfo.pieceY) {
            panelNumber = mPlayInfo.pieceX * mPlayInfo.pieceY - 1;
        }

        // 部位リストの生成
        initializePartList(patternNumber);

        // 全要素を初期化
        aryImgRes = new int[panelNumber][(partNumber + 1)];
        for (int[] imgRes : aryImgRes) {
            Arrays.fill(imgRes, SELECT_NONE);
        }

        // パネルをランダムに並べた配列
        int[] panels = c2r.getRandomPermutations(mPlayInfo.pieceX * mPlayInfo.pieceY - 1, panelNumber);
        //int[] parts = c2r.getRandomPermutations(partNumber, partNumber);

        Log.d("random", "pieces[]=" + Arrays.toString(panels));
        //Log.d("random", "pieces[]=" + Arrays.toString(parts));

        // 各パネルの各部位でランダムに模様を配置し、配列要素へ代入
        // 乱数(1～Nまで)
        // http://adash-android.jp.net/android%E3%81%A7%E4%B9%B1%E6%95%B0%E3%82%92%E5%8F%96%E5%BE%97/
        //Random random = new Random();
        //final int bound = 100;
        //int appear = 50;    // 出現率のパーセントの整数表記
        //int rate;           // 乱数値

        // 部位リストを各パネルに1個ずつ配置し、リストから取り除く
        for (int i = 0; i < panelNumber; i++) {
            PatternParts pp = listPP.get(0);
            int pattern = pp.getPattern();
            int part = pp.getPart();
            if ( aryImgRes[i][part] <= SELECT_NONE) {
                aryImgRes[i][part] = pattern * partNumber + part;
                listPP.remove(0);
            }
        }
        // パネル設置位置
        int j = partNumber;
        for (int i = 0; i < panelNumber; i++) {
            aryImgRes[i][j] = panels[i];
        }

        // debug 2次元配列の出力
        // https://teratail.com/questions/533
        Log.d("random", Arrays.deepToString(aryImgRes));
    }

    // さばいばる　部位リスト作成
    void initializePartList(int patternNumber) {
        final int partNumber = 4; // 固定
        // 部位リストのクリア
        listPP.clear();
        // 画面に出現する部位のリスト（１次元）を作る
        for (int i = 0; i < patternNumber; i++) {
            for (int j = 0; j < partNumber; j++) {
                for (int k = 0; k < COLOR_REPEAT; k++) {
                    listPP.add(new PatternParts(i,j));
                }
            }
        }
        // ランダムに並べ替え
        Collections.shuffle(listPP);
    }

    // さばいばる　部位追加
    private void addSurvivalParts(int patternNumber, int addNumber) {
        final int partNumber = 4; // 固定

        // 各パネルの各部位でランダムに模様を配置し、配列要素へ代入
        // 乱数(1～Nまで)
        // http://adash-android.jp.net/android%E3%81%A7%E4%B9%B1%E6%95%B0%E3%82%92%E5%8F%96%E5%BE%97/
        //Random random = new Random();
        //final int bound = 100;
        //int appear = 50;    // 出現率のパーセントの整数表記
        //int rate;           // 乱数値
        int pattern = SELECT_NONE;
        int part = SELECT_NONE;
        int addCount = 0;
        boolean flagAdd = false;

        // パネルの総数
        int panelNumber = mPlayInfo.pieceX * mPlayInfo.pieceY;

        // 部位の追加数だけ繰り返す
        for (int j = 0; j < addNumber; j++) {
            // パネルIdのランダムリスト
            int[] panels = c2r.getRandomPermutations(panelNumber, panelNumber);

            // 部位リストが空になったら再生成
            if (listPP.size() <= 0) {
                initializePartList(patternNumber);
            }

            // 追加する部位の取り出し
            PatternParts pp = listPP.get(0);
            pattern = pp.getPattern();
            part = pp.getPart();

            flagAdd = false;

            // 各パネルに対し、部位リスト要素を取り出し追加する
            for (int i = 0; i < panelNumber; i++) {

                CustomView cv = mImagePieces.get(panels[i]);
                // 非表示はスキップ
                if (cv.getVisibility() == View.INVISIBLE) {
                    continue;
                }

                // 各部位をチェック
                int resId = cv.getResId();
                // すでに部位があればスキップ
                if (aryImgRes[resId][part] > SELECT_NONE) {
                    continue;
                }

                // 部位を追加
                //Log.d("addPart","OLD: aryImgRes[resId="+resId+"][part="+part+"]="+aryImgRes[resId][part]);
                aryImgRes[resId][part] = pattern * partNumber + part;
                addCount++;
                flagAdd = true;
                //Log.d("addPart","NEW: aryImgRes[resId="+resId+"][part="+part+"]="+aryImgRes[resId][part]);

                // 画面を更新
                //180218 バグ。すでに模様のあるところに上書き発生を確認。要調査
                updateImagePart(panels[i],DIRECTION_NONE,part,aryImgRes[resId][part]);
                break;
            }
            // 結果をlistPPへ反映
            if (flagAdd) {
                // 追加できた
                listPP.remove(0);
            } else {
                // 追加できなかったので後回し
                // 無限ループ懸念
                listPP.add(pp);
                listPP.remove(0);
            }
        }

        Log.d("addSurvivalParts",addCount+" parts added.");
        // debug 2次元配列の出力
        // https://teratail.com/questions/533
        Log.d("random", Arrays.deepToString(aryImgRes));
    }
}
