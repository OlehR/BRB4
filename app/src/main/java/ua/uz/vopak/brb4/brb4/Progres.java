//Не використовується
package ua.uz.vopak.brb4.brb4;

import android.content.Context;
import android.widget.ProgressBar;

public class Progres {

    private ProgressBar progresBar;
    public Progres(ProgressBar parProgressBar)
    {
        progresBar = parProgressBar;
    }
    public void SetProgres(int progres)
    {
        progresBar.setProgress(progres);
    }
}
