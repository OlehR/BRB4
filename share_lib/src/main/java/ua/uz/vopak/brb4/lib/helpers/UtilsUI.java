package ua.uz.vopak.brb4.lib.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UtilsUI {
    Context context;
    public UtilsUI(Context pContext){context=pContext;}
    public void SetColor(ViewGroup pRoot, String pTextColor, String pBackgroundColor){

        final int childCount = pRoot.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = pRoot.getChildAt(i);
            if (child instanceof ViewGroup) {
                SetColor((ViewGroup) child, pTextColor,pBackgroundColor);
            }
            if (child instanceof TextView) {
                TextView v= (TextView)child;
                v.setTextColor(Color.parseColor(pTextColor));
                v.getBackground().setColorFilter(Color.parseColor(pBackgroundColor), PorterDuff.Mode.DARKEN);
                //v.setBackgroundColor(Color.parseColor(pBackgroundColor));
            }
        }

    }

    public void Dialog(String pHead,String pText) {
        new AlertDialog.Builder(context)
                .setTitle(pHead)
                .setMessage(pText)
                .setPositiveButton(android.R.string.ok, null)
                .create().show();
    }


}
