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
        builder.setMessage("中断データがあります。再開しますか？\n再開しない場合、中断データは削除されます。")
                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // アクティビティを参照
                        TitleActivity callingActivity = (TitleActivity) getActivity();
                        // コールバック
                        // http://www.ipentec.com/document/android-custom-dialog-using-dialogfragment-return-value
                        callingActivity.onReturnValue(MainActivity.RETURN_YES);
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
