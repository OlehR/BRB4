package ua.uz.vopak.brb4.brb4;

import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class HandlerPC {
    private PriceCheckerActivity priceCheckerActivity;
    public HandlerPC(PriceCheckerActivity pPriceCheckerActivity){priceCheckerActivity=pPriceCheckerActivity;}
    public void OnClickAddPrintBlock(final LabelInfo pLI)
    {
        pLI.config.NumberPackege++;

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date today = Calendar.getInstance().getTime();
        final String todayAsString = df.format(today);
        //new AsyncConfigPairAdd(worker).execute("NumberPackege",todayAsString+GlobalConfig.NumberPackege.toString());
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                priceCheckerActivity.config.Worker.AddConfigPair("NumberPackege",todayAsString+pLI.strNumberPackege());
                return null;
            }
        }).execute();

        pLI.ListPackege.add(pLI.strNumberPackege() + "-" +  "0/0" );
        pLI.ListPackegeIdx.set(pLI.ListPackege.size()-1);
        //priceCheckerActivity.LoadSpinner();
        priceCheckerActivity.binding.invalidateAll();
    }
    public void OnClickChangePrintType(LabelInfo pLI)
    {
        pLI.IsShort=!pLI.IsShort;
        priceCheckerActivity.binding.invalidateAll();
    }
    public void OnClickChangePrintColorType(LabelInfo pLI)
    {
        pLI.SetPrintType();
        priceCheckerActivity.binding.invalidateAll();
    }

    public void OnClickPrintBlock(LabelInfo pLI)
    {
        int varPackege= Integer.valueOf(pLI.ListPackege.get(pLI.ListPackegeIdx.get()).split("-")[0]);
        priceCheckerActivity.BL.printPackage(pLI.printType,varPackege);
        priceCheckerActivity.binding.invalidateAll();
    }

    public void OnClickBarCode(View v,LabelInfo pLI) {
/*
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.requestFocusFromTouch();
        v.setFocusableInTouchMode(false);*/
        pLI.BarCode.set("");
    }

    public void OnClickNumberOfReplenishment(View v,LabelInfo pLI) {
        //pLI.NumberOfReplenishment.set("");
    }


}
