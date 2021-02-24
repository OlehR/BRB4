package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.brb4.models.DocModel;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.Reason;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.helpers.DataBaseHelper;
import ua.uz.vopak.brb4.lib.models.PriceBarCode;
import ua.uz.vopak.brb4.lib.models.Result;

public class SQLiteAdapter
{
    protected static final String TAG = "BRB4/SQLiteAdapter";
  //  SimpleDateFormat formatterDate= new SimpleDateFormat("yyyy-MM-dd");
    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;
    GlobalConfig config = GlobalConfig.instance();

    public SQLiteAdapter(Context context)    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public SQLiteDatabase GetDB(){return mDb;}

    public SQLiteAdapter createDatabase() throws SQLException    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public SQLiteAdapter open() throws SQLException    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    public void AddConfigPair(String name, String value) {
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("NAME_VAR", name);
            values.put("DATA_VAR", value);
            db.replace("CONFIG", null, values);
        }
        catch (Exception e)
        {
            String s=e.getMessage();
        }
    }

    public String GetConfigPair(String name) {
        Cursor mCur;
        String value = "";
        String sql = "SELECT DATA_VAR FROM CONFIG WHERE NAME_VAR = '"+name+"'";

        mCur = mDb.rawQuery(sql, null);
        if (mCur!=null && mCur.getCount() > 0) {
            mCur.moveToFirst();
            value = mCur.getString(0);
        }

        return value;
    }

    public void InsLogPrice(String pBarCode,Integer pStatus, Integer pActionType, Integer pPackageNumber, Integer pCodeWarees,String pArticle,Integer pLineNumber) {
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("bar_code", pBarCode);
            values.put("Status", pStatus);
            values.put("action_type", pActionType);
            values.put("package_number", pPackageNumber);
            values.put("code_wares", pCodeWarees);
            values.put("Line_Number", pLineNumber);
            values.put("Article", pArticle);

            db.insert("LogPrice", null, values);
        }
        catch (Exception e)
        {
            String s=e.getMessage();
        }
     }

    public int[] GetCountScanCode() {
        int[] varRes = {0,0};
    try
    {
        String sql ="select count(*),sum(case when Status=0 then 1 else 0 end) from   LogPrice where is_send=0";

        Cursor mCur = mDb.rawQuery(sql, null);
        if (mCur!=null)
        {
            mCur.moveToFirst() ;
            varRes[0]=mCur.getInt(0);
            varRes[1]=mCur.getInt(1);
        }

    }
    catch (SQLException mSQLException)
    {
        Log.e(TAG, "GetCountScanCode >>"+ mSQLException.toString());
        //throw mSQLException;
    }
        return varRes;
    }

    public boolean LoadDataDoc(String parSQL, ObservableInt pProgress)    {
        int varN=mDb.getVersion();
        try {
            /*String sql ="select count(*) from  bar_code";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
            }*/

            String[] varS=parSQL.split(";;;");
            int l=varS.length;
            int coef= (l+1)/40;
            for(int i=0;i<l;i++)
            {
                if(varS[i].trim() != null && !varS[i].trim().isEmpty() && varS[i].length()>10)
                    mDb.execSQL(varS[i]);
                if(pProgress!=null)
                    pProgress.set(50+coef*i);
            }
/*
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
            }
            int i=varN++;*/
            if(pProgress!=null)
                pProgress.set(100);
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "LoadDataDoc=>"+mSQLException.getMessage());
            if(pProgress!=null)
                pProgress.set(0);
            String m=mSQLException.getMessage();
            return false;
         }

        return true;
    }

    public HashMap<String,String[]> getPrintBlockItemsCount(){
        HashMap<String,String[]> data = new HashMap<String,String[]>();
        Cursor mCur;

        String sql = "select package_number,count(DISTINCT case when action_type in (1,2) then null else code_wares end) as normal,count(DISTINCT case when action_type in (1,2) then code_wares end) as yellow " +
                "from LogPrice WHERE  Status< 0 AND date(DT_insert) > date('now','-1 day') " +
                "GROUP BY package_number";

        mCur = mDb.rawQuery(sql, null);
        if(mCur != null){
            while (mCur.moveToNext()){
                data.put(mCur.getString(0),new String[]{mCur.getString(1),mCur.getString(2)});
            }
        }
        return data;
    }

    public List<String> getPrintPackageCodeWares(Integer actionType, Integer packageNumber,boolean IsMultyLabel){
        Cursor mCur;
        List<String> data = new ArrayList<String>();
        String _actionType = "";
        if(actionType == 0)
            _actionType = " AND action_type NOT IN(1,2)";
        else
            if(actionType == 1)
                _actionType = " AND action_type IN(1,2)";

        String sql = "SELECT "+(IsMultyLabel?"":"DISTINCT")+ " code_wares FROM LogPrice WHERE code_wares>0 and package_number ="+packageNumber+" AND Status < 0 AND date(DT_insert) > date('now','-1 day')"+_actionType;

        mCur = mDb.rawQuery(sql, null);
        if (mCur!=null && mCur.getCount() > 0) {
            while (mCur.moveToNext()){
                data.add(mCur.getString(0));
            }
        }
        return data;
    }

    public List<LogPrice> GetSendData(int pLimit) {
        int varN;
        Cursor mCur;
        List<LogPrice> list = new ArrayList<>();
        try
        {
            String sql ="select count(*) from   LogPrice where is_send=-1";

            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
                if (varN < pLimit) {
//                    sql = "UPDATE LogPrice SET is_send=-1 WHERE `rowid` IN (SELECT `rowid` FROM LogPrice WHERE is_send=0 LIMIT 100)";
//                    mDb.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("is_send",-1);
                    mDb.update("LogPrice",cv,"rowid  IN (SELECT rowid FROM LogPrice WHERE is_send=0 LIMIT " +pLimit+")",null);
                }
            }

            sql ="select bar_code,Status,DT_insert,package_number,is_send,action_type,code_wares,article,Line_Number,Number_Of_Replenishment from LogPrice where is_send=-1";
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToFirst() ;
                while (mCur.moveToNext())
                { LogPrice el = new LogPrice(mCur);
                    list.add(el);
                }
            }
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "GetSendData >>"+ mSQLException.toString());
            //throw mSQLException;
        }
        return list;
    }

    public void AfterSendData() {
        ContentValues cv = new ContentValues();
        cv.put("is_send",1);
        mDb.update("LogPrice",cv,"is_send=-1",null);
    }
    //Пповнення стелажу СКЮ
    public void UpdateReplenishment(int pLineNumber,double pNumberOfReplenishment){
        try {
            ContentValues values = new ContentValues();
            values.put("Number_Of_Replenishment", pNumberOfReplenishment);
            mDb.update("LogPrice", values, " date('now','-1 day') and is_send=0 and Line_Number=" + pLineNumber,null);
        }
        catch (Exception e)
        {
            String s=e.getMessage();
            Log.e(TAG, "UpdateReplenishment >>"+ e.toString());
        }

    }
    //Робота з документами.
    public List<DocumentModel> GetDocumentList(int pTypeDoc,String pBarCode,String pExtInfo) {

        DocSetting DS=config.GetDocSetting(pTypeDoc);

        List<DocumentModel> model = new ArrayList<DocumentModel>();
        Cursor mCur;
        String sql=null;

        try {
            sql = "SELECT date_doc,type_doc,number_doc,ext_info,name_user,bar_code,description,dt_insert,state \n"+
                    ", (select count(*)  from DOC_WARES_sample dws join wares w on (dws.code_wares=w.CODE_WARES and w.CODE_UNIT="+config.GetCodeUnitWeight()+") where dws.type_doc=d.type_doc and dws.number_doc=d.number_doc ) as Weight\n" +
                    ", (select count(*)  from DOC_WARES_sample dws join wares w on (dws.code_wares=w.CODE_WARES and w.CODE_UNIT<>"+config.GetCodeUnitWeight()+") where dws.type_doc=d.type_doc and dws.number_doc=d.number_doc ) as NoWeight\n"+
                    "FROM DOC d WHERE d.state>=0 and type_doc = '"+pTypeDoc+"'"+
                    " AND date_doc >= date(datetime(CURRENT_TIMESTAMP,'-"+DS.DayBefore+" day')) \n" +//AND datetime(CURRENT_TIMESTAMP)
                    (pExtInfo==null?"":" and ext_info like'%"+ pExtInfo.trim()+"%'") +
                    (pBarCode==null?"":" and bar_code like'%"+ pBarCode.trim()+"%'") +
                    (config.IsDebug ? " limit 10" :"");
            mCur = mDb.rawQuery(sql, null);
            // якщо нічого не найшли і штрихкод не порожній шукаємо по товарам.
            if (pBarCode!=null && !pBarCode.isEmpty() && (mCur==null || mCur.getCount() <= 0) ) {
                sql="SELECT DISTINCT d.date_doc,d.type_doc,d.number_doc,d.ext_info,d.name_user,d.bar_code,d.description,d.dt_insert,d.state -- ,bc.BAR_CODE, dw.*\n" +
                        ", (select count(*)  from DOC_WARES_sample dws join wares w on (dws.code_wares=w.CODE_WARES and w.CODE_UNIT="+config.GetCodeUnitWeight()+") where dws.type_doc=d.type_doc and dws.number_doc=d.number_doc ) as Weight\n" +
                        ", (select count(*)  from DOC_WARES_sample dws join wares w on (dws.code_wares=w.CODE_WARES and w.CODE_UNIT<>"+config.GetCodeUnitWeight()+") where dws.type_doc=d.type_doc and dws.number_doc=d.number_doc ) as NoWeight\n"+
                        "FROM DOC d \n" +
                        "Join DOC_WARES_SAMPLE dw on dw.number_doc=d.number_doc and dw.type_doc=d.type_doc\n" +
                        "join bar_code bc on dw.code_wares=bc.CODE_WARES\n" +
                        "WHERE d.state>=0 and d.type_doc = '"+pTypeDoc+"'"+
                        " AND d.date_doc BETWEEN date(datetime(CURRENT_TIMESTAMP,'-"+DS.DayBefore+" day')) AND datetime(CURRENT_TIMESTAMP)"+
                        "and bc.BAR_CODE= '"+pBarCode+"'" + (config.IsDebug ? " limit 10" :"");
                mCur = mDb.rawQuery(sql, null);
            }

            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    DocumentModel document = new DocumentModel();
                    document.DateDoc = mCur.getString(0);
                    document.TypeDoc = mCur.getInt(1);
                    document.NumberDoc = mCur.getString(2);
                    document.ExtInfo = mCur.getString(3);
                    document.NameUser = mCur.getString(4);
                    document.BarCode = mCur.getString(5);
                    document.Description = mCur.getString(6);
                    document.DateInsert = mCur.getString(7);
                    document.State = mCur.getInt(8);
                    int Weight=mCur.getInt(9);
                    int NoWeight=mCur.getInt(10);
                    document.WaresType = (Weight>0 && NoWeight>0)? 0: (Weight>0?2:1);
                    model.add(document);
                }
            }
        }catch (Exception e){
            Log.e(TAG, "GetDocumentList >>"+  e.getMessage());
        }
        return model;
    }

    public int GetStateDoc(int pTypeDoc, String pNumberDoc) {
        int res=0;
        String sql="select state from Doc d  where  d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" ;
        Cursor mCur;
        try {
            mCur = mDb.rawQuery(sql, null);

            if (mCur!=null && mCur.getCount() > 0) {
                mCur.moveToFirst();
            res=mCur.getInt(0);
            }
        }catch (Exception e){
            Log.e(TAG, "GetStateDoc >>"+  e.getMessage());
        }
        return res;

    }
    public List<WaresItemModel> GetDocWares(int pTypeDoc, String pNumberDoc, int pTypeResult, eTypeOrder pTypeOrder) {
        DocSetting DS=config.GetDocSetting(pTypeDoc);
        List<WaresItemModel> model = new ArrayList<>();
        Cursor mCur;
        String sql="";
        String OrderQuery="11 desc,1";
        if(pTypeOrder==eTypeOrder.Name)
            OrderQuery="11 desc,3";

        String Color=" ,0 as Ord";
        if(DS.TypeColor==1) {
            Color=", case when dws.code_wares is null then 2 else 0 end as Ord\n";
        } else
        if(DS.TypeColor==2) {
            Color=", case when coalesce(dws.quantity,0) - coalesce(dw1.quantity_input,0) <0 then 3 \n" +
                    "        when coalesce(dws.quantity,0) - coalesce(dw1.quantity_input,0) >0 then 2\n" +
                    "        when coalesce(dws.quantity,0) - coalesce(dw1.quantity_input,0)=0 and quantity_reason>0 then 1\n" +
                    "   else 0 end as Ord\n";
        }

        if(pTypeResult==1)
          sql = " select dw1.order_doc, w.CODE_WARES,w.NAME_WARES,coalesce(dws.quantity,0) as quantity_order,coalesce(dw1.quantity_input,0) as quantity_input, coalesce(dws.quantity_min,0) as quantity_min, coalesce(dws.quantity_max,0) as quantity_max ,coalesce(d.Is_Control,0) as Is_Control, coalesce(dw1.quantity_old,0) as quantity_old\n" +
                ",dw1.quantity_reason as quantity_reason \n" +
                  Color+
                  ",w.code_unit\n"+
                  "    from Doc d  \n" +
                "    join (select dw.type_doc ,dw.number_doc, dw.code_wares, sum(dw.quantity) as quantity_input,max(dw.order_doc) as order_doc,sum(quantity_old) as quantity_old,  sum(case when dw.CODE_Reason>0 then  dw.quantity else 0 end) as quantity_reason  from doc_wares dw group by dw.type_doc ,dw.number_doc,code_wares) dw1 on (dw1.number_doc = d.number_doc and d.type_doc=dw1.type_doc)\n" +
                "    join wares w on dw1.code_wares = w.code_wares \n" +
                "    left join (\n" +
                  "    select  dws.type_doc ,dws.number_doc, dws.code_wares, sum(dws.quantity) as quantity,  min(dws.quantity_min) as quantity_min, max(dws.quantity_max) as quantity_max  from   DOC_WARES_sample dws   group by dws.type_doc ,dws.number_doc,dws.code_wares\n" +
                  "    ) as dws on d.number_doc = dws.number_doc and d.type_doc=dws.type_doc and dws.code_wares = w.code_wares\n" +
                "    where d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" +
                " union all\n" + 
                " select dws.order_doc+100000, w.CODE_WARES,w.NAME_WARES,coalesce(dws.quantity,0) as quantity_order,coalesce(dw1.quantity_input,0) as quantity_input, coalesce(dws.quantity_min,0) as quantity_min, coalesce(dws.quantity_max,0) as quantity_max ,coalesce(d.Is_Control,0) as Is_Control, coalesce(dw1.quantity_old,0) as quantity_old\n" +
                "     ,0 as  quantity_reason "+
                ", 3 as Ord\n"+
                ",w.code_unit\n"+
                "    from Doc d  \n" +
                "    join DOC_WARES_sample dws on d.number_doc = dws.number_doc and d.type_doc=dws.type_doc --and dws.code_wares = w.code_wares\n" +
                "    join wares w on dws.code_wares = w.code_wares \n" +
                "    left join (select dw.type_doc ,dw.number_doc, dw.code_wares, sum(dw.quantity) as quantity_input,sum(dw.quantity_old) as quantity_old from doc_wares dw group by dw.type_doc ,dw.number_doc,code_wares) dw1 on (dw1.number_doc = d.number_doc and d.type_doc=dw1.type_doc and dw1.code_wares = w.code_wares)\n" +
                "    where dw1.type_doc is null and d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" +
                " order by "+OrderQuery;

        if(pTypeResult==2)
            sql="select dw1.order_doc, w.CODE_WARES,w.NAME_WARES,coalesce(dws.quantity,0) as quantity_order,coalesce(dw1.quantity,0) as quantity_input, coalesce(dws.quantity_min,0) as quantity_min, coalesce(dws.quantity_max,0) as quantity_max ,coalesce(d.Is_Control,0) as Is_Control, coalesce(dw1.quantity_old,0) as quantity_old,dw1.CODE_Reason \n" +
                    ",0 as Ord,w.code_unit\n"+
                    "    from Doc d  \n" +
                    "    join doc_wares dw1 on (dw1.number_doc = d.number_doc and d.type_doc=dw1.type_doc)\n" +
                    "    join wares w on dw1.code_wares = w.code_wares \n" +
                    "    left join (\n" +
                    "    select  dws.type_doc ,dws.number_doc, dws.code_wares, sum(dws.quantity) as quantity,  min(dws.quantity_min) as quantity_min, max(dws.quantity_max) as quantity_max  from   DOC_WARES_sample dws   group by dws.type_doc ,dws.number_doc,dws.code_wares\n" +
                    "    ) as dws on d.number_doc = dws.number_doc and d.type_doc=dws.type_doc and dws.code_wares = w.code_wares\n"+
                    "    where d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" +
                    " order by 1,2";


        DocSetting DocSetting=config.GetDocSetting(pTypeDoc);
        try {
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    WaresItemModel WaresModel = new WaresItemModel();
                    WaresModel.TypeDoc=pTypeDoc;
                    WaresModel.DocSetting=DocSetting;
                    WaresModel.OrderDoc = mCur.getInt(0);
                    WaresModel.CodeWares = mCur.getInt(1);
                    WaresModel.NameWares = mCur.getString(2);
                    WaresModel.QuantityOrder = mCur.getDouble(3);
                    WaresModel.InputQuantity = mCur.getDouble(4);
                    WaresModel.QuantityMin = mCur.getDouble(5);
                    WaresModel.QuantityMax=(mCur.getInt(7)==1?mCur.getDouble(6):Double.MAX_VALUE);
                    WaresModel.QuantityOld=mCur.getDouble(8);
                    if(pTypeResult==2)
                        WaresModel.CodeReason=  mCur.getInt(9);
                    else {
                        WaresModel.QuantityReason = mCur.getDouble(9);
                    }
                    WaresModel.Ord=mCur.getInt(10);
                    WaresModel.CodeUnit=mCur.getInt(11);
                    model.add(WaresModel);
                }
            }
        }catch (Exception e){
            Log.e(TAG, "GetDocWares >>"+  e.getMessage());
        }
        return model;
    }

    public WaresItemModel GetScanData(int TypeDoc, String DocNumber,String number) {
        number=number.trim();
        WaresItemModel model = null;
        Cursor mCur=null;
        String sql;
        Log.d(TAG, "Find in DB  >>"+ number );
         Integer intNum = 0;
        boolean isBarCode = true;
        if (number.length() <= 8 && !number.equals("")) {
            intNum = Integer.parseInt(number);
            isBarCode = (intNum.toString().length() >= 8);
        }
   /*     else {
            PriceBarCode v = new PriceBarCode(number,config.Company);
            if(v.Code>0) {
                number=Integer.toString( v.Code);
                isBarCode=false;
            }
        }*/
        try {
            if (isBarCode) {
                sql = "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,bc.CODE_UNIT, ud.ABR_UNIT , bc.BAR_CODE  ,w.CODE_UNIT as BASE_CODE_UNIT " +
                        "from BAR_CODE bc " +
                        "join ADDITION_UNIT au on bc.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=bc.CODE_UNIT " +
                        "join wares w on w.CODE_WARES=bc.CODE_WARES " +
                        "join UNIT_DIMENSION ud on bc.CODE_UNIT=ud.CODE_UNIT " +
                        "where bc.BAR_CODE='" + number + "'";
                mCur = mDb.rawQuery(sql, null);
                if (mCur == null || mCur.getCount() == 0) {
                    sql = "select bc.code_wares,bc.BAR_CODE from BAR_CODE bc \n" +
                            " join wares w on bc.code_wares=w.code_wares and w.code_unit=" + config.GetCodeUnitWeight() + "\n" +
                            " where substr(bc.BAR_CODE,1,6)='" + number.substring(0, 6) + "'";
                    mCur = mDb.rawQuery(sql, null);
                    if (mCur == null || mCur.getCount() == 0) {

                        PriceBarCode v = new PriceBarCode(number, config.Company);
                        if (v.Code > 0) {
                            number = Integer.toString(v.Code);
                            return GetScanData(TypeDoc, DocNumber, number);
                        }

                    } else {
                        while (mCur.moveToNext()) {
                            String CodeWares = mCur.getString(0);
                            String BarCode = mCur.getString(1);
                            if (number.substring(0, BarCode.length()).equals(BarCode)) {

                                WaresItemModel res = GetScanData(TypeDoc, DocNumber, CodeWares);
                                try {
                                    String Weight;
                                    Weight = number.substring(8, 12);
                                    res.QuantityBarCode = Double.parseDouble(Weight) / 1000;
                                } catch (NumberFormatException e) {
                                    res.QuantityBarCode = 0d;
                                }
                                return res;
                            }
                        }
                        mCur=null;
                    }
                }
            }else {
                    String Find = config.Company == eCompany.SevenEleven ? "w.code_wares=" + number : "w.ARTICL='" + number + "'";
                    sql = "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,w.CODE_UNIT, ud.ABR_UNIT , '' as BAR_CODE  ,w.CODE_UNIT as BASE_CODE_UNIT " +
                            "from WARES w " +
                            "join ADDITION_UNIT au on w.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=w.CODE_UNIT " +
                            "join UNIT_DIMENSION ud on w.CODE_UNIT=ud.CODE_UNIT " +
                            "where " + Find;
                    mCur = mDb.rawQuery(sql, null);

            }

            if (mCur != null && mCur.getCount() > 0) {
                mCur.moveToFirst();
                model = new WaresItemModel();

                model.CodeWares = mCur.getInt(0);
                model.NameWares = mCur.getString(1);
                model.Coefficient = mCur.getInt(2);
                model.CodeUnit = mCur.getInt(3);
                model.NameUnit = mCur.getString(4);
                model.BarCode = mCur.getString(5);
                model.BaseCodeUnit = mCur.getInt(6);
            }
        } catch (Exception e) {
            Log.e(TAG, "GetScanData >>"+  e.getMessage());

        }
        if (model != null && DocNumber!=null) {
            sql = "select coalesce(d.Is_Control,0) as Is_Control, coalesce(quantity_max,0) as quantity_max, coalesce(quantity,0) as quantity, case when dws.Type_doc is null then 0 else 1 end as Find from DOC d\n" +
                    " left join DOC_WARES_sample dws on d.Type_doc=dws.Type_doc and d.number_doc=dws.number_doc and dws.code_wares=" + model.CodeWares +
                    " \nwhere  d.Type_doc=" + TypeDoc + " and d.number_doc=\"" + DocNumber + "\"";
            try {
                mCur = mDb.rawQuery(sql, null);
                if (mCur != null && mCur.getCount() > 0) {
                    mCur.moveToFirst();
                    model.QuantityMax=(mCur.getInt(0)==1?mCur.getDouble(1):Double.MAX_VALUE);
                    model.QuantityOrder=mCur.getInt(2);
                    model.IsRecord= mCur.getInt(3)==1;

                }
            } catch (Exception e) {
                Log.e(TAG, "GetScanData >>"+  e.getMessage());
            }

        }
        Log.d(TAG, "Found in DB  >>"+ (model==null ? "Not Found": model.NameWares) );
        return model;
    }

    public Reason[] GetReason() {
        ArrayList<Reason> model = new ArrayList<Reason>(){{add(new Reason(0,"Ok"));}};
        Cursor mCur;
        String sql ="select CODE_Reason as СodeReason, NAME_Reason as NameReason from reason";
        try {
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    model.add(new Reason(mCur.getInt(0),mCur.getString(1)));
                }
            }
        }catch (Exception e){
            Log.e(TAG, "GetReason>>"+  e.getMessage());
        }
        return model.toArray(new Reason[model.size()]);
    }

    public void UpdateDocState(int pState,int pTypeDoc, String pNumberDoc){
        try {
            ContentValues values = new ContentValues();
            values.put("state", pState);
            mDb.update("DOC", values, "Type_doc=" + pTypeDoc + " and number_doc='" + pNumberDoc + "'", null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "UpdateDocState >>"+  e.getMessage());

        }
    }

    public void SetNullableWares(int parTypeDoc,String parNumberDoc,int CodeWares ){
        long result = -1;
        String Where = "code_wares = "+ CodeWares +" and type_doc="+ parTypeDoc+" and number_doc=\""+ parNumberDoc+"\"";
        try {
            mDb.execSQL("update DOC_WARES set quantity_old=quantity where quantity>0 and "+Where);
            ContentValues cv = new ContentValues();
            cv.put("quantity",0);
            mDb.update("DOC_WARES",cv,Where,null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "SetNullableWares >>"+  e.getMessage());
        }
    }

    public Result SaveDocWares(int pTypeDoc, String pNumberDoc, int pCodeWares, int pOrderDoc, Double pQuantity, int pCodeReason ){
        long result = -1;
        String s = "Ok";
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("type_doc", pTypeDoc);
            values.put("number_doc", pNumberDoc);
            values.put("code_wares", pCodeWares);
            values.put("order_doc", pOrderDoc);
            values.put("quantity", pQuantity);
            values.put("quantity_old", 0);
            values.put("CODE_Reason", pCodeReason);
            result = db.insert("DOC_WARES", null, values);
        }
        catch (Exception e)
        {
            s=e.getMessage();
            Log.e(TAG, "SaveDocWares >>"+  e.getMessage());
        }
       return new Result((int)result,s);
    }

    public boolean SaveDocs(Doc pDoc ) {
        long result = -1;
        int State=GetStateDoc(pDoc.TypeDoc,pDoc.NumberDoc);
        if(State<0)
            State=0;
        ContentValues values = new ContentValues();
        values.put("type_doc", pDoc.TypeDoc);
        values.put("number_doc", pDoc.NumberDoc);
        values.put("date_doc", pDoc.DateDoc.toString());
        values.put("Is_Control", pDoc.IsControl);
        values.put("ext_info", pDoc.ExtInfo);
        values.put("name_user", pDoc.NameUser);
        values.put("bar_code", pDoc.BarCode);
        values.put("description", pDoc.Description);
        values.put("number_doc_1C", pDoc.NumberDoc1C);
        values.put("Date_Out_Invoice", pDoc.DateOutInvoice);
        values.put("Number_Out_Invoice", pDoc.NumberOutInvoice);

        values.put("state",State);
        result = mDb.replace("DOC", null, values);
    return result != -1;
    }

    public boolean SaveDocWaresSample(DocWaresSample pDWS ) {
        long result = -1;
        ContentValues values = new ContentValues();
        values.put("type_doc", pDWS.TypeDoc);
        values.put("number_doc", pDWS.NumberDoc);
        values.put("order_doc", pDWS.OrderDoc);
        values.put("code_wares", pDWS.CodeWares);
        values.put("quantity", pDWS.Quantity);
        values.put("quantity_min", pDWS.QuantityMin);
        values.put("quantity_max", pDWS.QuantityMax);

        result = mDb.replace("DOC_WARES_sample", null, values);
        return result != -1;
    }

    public void SaveDocOut(Doc pDoc ){
        long result = -1;
        String Where = "type_doc="+ pDoc.TypeDoc+" and number_doc=\""+ pDoc.NumberDoc+"\"";
        try {

            ContentValues cv = new ContentValues();
            cv.put("Date_Out_Invoice",pDoc.DateOutInvoice.toString());
            cv.put("Number_Out_Invoice",pDoc.NumberOutInvoice);
            mDb.update("DOC",cv,Where,null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "SaveDocOut >>"+  e.getMessage());
        }
    }

    public DocModel GetDocOut(int pTypeDoc, String pNumberDoc){
        DocModel result = new DocModel(pTypeDoc,pNumberDoc);
        String sql = "select Date_Out_Invoice,Number_Out_Invoice from doc where type_doc="+ pTypeDoc+" and number_doc=\""+ pNumberDoc+"\"";
        try {
              Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur != null && mCur.getCount() > 0) {
                mCur.moveToFirst();
               /* try {
                    result.DateOutInvoice = formatterDate.parse(  mCur.getString(0));
                }catch (Exception e)
                {
                    result.DateOutInvoice= formatterDate.parse(formatterDate.format(Calendar.getInstance().getTime()));
                }*/
                result.DateOutInvoice = mCur.getString(0);
                result.NumberOutInvoice =mCur.getString(1);

            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "GetDocOut >>"+  e.getMessage());
        }
        return result;
    }
}
