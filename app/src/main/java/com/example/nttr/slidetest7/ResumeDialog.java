package com.example.nttr.slidetest7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by tamano on 2018/03/11. Copy from SuspendDialog.java
 */

// ダイアログ
// https://qiita.com/ux_design_tokyo/items/61ca074566d1570b37d3
// インポートするsuper classの選定について
// http://furudate.hatenablog.com/entry/2014/01/09/162421
public class ResumeDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("＜かくにん＞")
                .setMessage("プレイ中のデータがあります。\n再開しますか？別のプレイを開始すると消去されます。")
                .setPositiveButton("さいかい", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // アクティビティを参照
                        TitleActivity callingActivity = (TitleActivity) getActivity();
                        // コールバック
                        // http://www.ipentec.com/document/android-custom-dialog-using-dialogfragment-return-value
                        callingActivity.onReturnValue(TitleActivity.RETURN_RESUME);
                    }
                })
                .setNegativeButton("はじめから", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // アクティビティを参照
                        TitleActivity callingActivity = (TitleActivity) getActivity();
                        // コールバック
                        // http://www.ipentec.com/document/android-custom-dialog-using-dialogfragment-return-value
                        callingActivity.onReturnValue(TitleActivity.RETURN_NEW);
                    }
                })
                .setNeutralButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled the dialog
                    }
                });
                // TODO 起動直後だけ「はじめから」は要らないが、他は同じ

        // Create the AlertDialog object and return it
        return builder.create();
    }
}