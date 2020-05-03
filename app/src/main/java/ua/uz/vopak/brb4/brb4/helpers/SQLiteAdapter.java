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

import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;

public class SQLiteAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;
    GlobalConfig config = GlobalConfig.instance();

    public SQLiteAdapter(Context context)    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

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
        Log.e(TAG, "getTestData >>"+ mSQLException.toString());
        //throw mSQLException;
    }
        return varRes;
    }

    public boolean LoadDataDoc(String parSQL)    {
        int varN=mDb.getVersion();
        try {
            /*String sql ="select count(*) from  bar_code";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
            }*/

            String[] varS=parSQL.split(";;;");
            for(int i=0;i<varS.length;i++)
            {
                if(varS[i].trim() != null && !varS[i].trim().isEmpty() && varS[i].length()>10)
                    mDb.execSQL(varS[i]);
            }
/*
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
            }
            int i=varN++;*/
        }
        catch (SQLException mSQLException)
        {
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

    public List<String> getPrintPackageCodeWares(Integer actionType, Integer packageNumber){
        Cursor mCur;
        List<String> data = new ArrayList<String>();
        String _actionType = "";
        if(actionType == 0)
            _actionType = " AND action_type NOT IN(1,2)";
        else
            if(actionType == 1)
                _actionType = " AND action_type IN(1,2)";

        String sql = "SELECT DISTINCT code_wares FROM LogPrice WHERE code_wares>0 and package_number ="+packageNumber+" AND Status < 0 AND date(DT_insert) > date('now','-1 day')"+_actionType;

        mCur = mDb.rawQuery(sql, null);
        if (mCur!=null && mCur.getCount() > 0) {
            while (mCur.moveToNext()){
                data.add(mCur.getString(0));
            }
        }
        return data;
    }

    public List<LogPrice> GetSendData() {
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
                if (varN == 0) {
//                    sql = "UPDATE LogPrice SET is_send=-1 WHERE `rowid` IN (SELECT `rowid` FROM LogPrice WHERE is_send=0 LIMIT 100)";
//                    mDb.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("is_send",-1);
                    mDb.update("LogPrice",cv,"rowid  IN (SELECT rowid FROM LogPrice WHERE is_send=0 LIMIT 100)",null);
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
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            //throw mSQLException;
        }
        return list;
    }

    public void AfterSendData() {
        ContentValues cv = new ContentValues();
        cv.put("is_send",1);
        mDb.update("LogPrice",cv,"is_send=-1",null);
    }
    public void UpdateReplenishment(int pLineNumber,double pNumberOfReplenishment){
        try {
            ContentValues values = new ContentValues();
            values.put("Number_Of_Replenishment", pNumberOfReplenishment);
            mDb.update("LogPrice", values, " date('now','-1 day') and is_send=0 and Line_Number=" + pLineNumber,null);
        }
        catch (Exception e)
        {
            String s=e.getMessage();
        }

    }


    //Робота з документами.
    public List<DocumentModel> GetDocumentList(int TypeDoc,String parBarCode) {

        List<DocumentModel> model = new ArrayList<DocumentModel>();
        Cursor mCur;
        String sql;
        if(parBarCode==null||parBarCode.isEmpty())
            sql = "SELECT date_doc,type_doc,number_doc,ext_info,name_user,bar_code,description,dt_insert,state FROM DOC WHERE type_doc = '"+TypeDoc+"'"+
                    " AND date_doc BETWEEN datetime(CURRENT_TIMESTAMP,'-2 day') AND datetime(CURRENT_TIMESTAMP)" + (config.IsDebug ? " limit 10" :"");
        else
            sql="SELECT DISTINCT d.date_doc,d.type_doc,d.number_doc,d.ext_info,d.name_user,d.bar_code,d.description,d.dt_insert,d.state -- ,bc.BAR_CODE, dw.*\n" +
                    "FROM DOC d \n" +
                    "Join DOC_WARES_SAMPLE dw on dw.number_doc=d.number_doc and dw.type_doc=d.type_doc\n" +
                    "join bar_code bc on dw.code_wares=bc.CODE_WARES\n" +
                    "WHERE d.type_doc = '"+TypeDoc+"'"+
                    " AND d.date_doc BETWEEN datetime(CURRENT_TIMESTAMP,'-2 day') AND datetime(CURRENT_TIMESTAMP)"+
                    "and bc.BAR_CODE= '"+parBarCode+"'" + (config.IsDebug ? " limit 10" :"");

        try {
            //mDb.delete("INVENTORY_WARES", null, null);
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    DocumentModel document = new DocumentModel();
                    document.DateDoc = mCur.getString(0);
                    document.TypeDoc = mCur.getString(1);
                    document.NumberDoc = mCur.getString(2);
                    document.ExtInfo = mCur.getString(3);
                    document.NameUser = mCur.getString(4);
                    document.BarCode = mCur.getString(5);
                    document.Description = mCur.getString(6);
                    //Костиль для вагового товару.
                    document.WaresType = document.Description.substring(0,1);
                    document.Description = document.Description.replace(document.WaresType,"");
                    document.DateInsert = mCur.getString(7);
                    document.State = mCur.getString(8);
                    model.add(document);
                }
            }
        }catch (Exception e){
            ;            e.getMessage();
        }
        return model;
    }

    public List<WaresItemModel> GetDocWares(int pTypeDoc,String pNumberDoc,int pTypeResult) {
        List<WaresItemModel> model = new ArrayList<>();
        Cursor mCur;
        String sql="";
        if(pTypeResult==1)
          sql = " select dw1.order_doc, w.CODE_WARES,w.NAME_WARES,coalesce(dws.quantity,0) as quantity_order,coalesce(dw1.quantity_input,0) as quantity_input, coalesce(dws.quantity_min,0) as quantity_min, coalesce(dws.quantity_max,0) as quantity_max ,coalesce(d.Is_Control,0) as Is_Control, coalesce(dw1.quantity_old,0) as quantity_old\n" +
                "    from Doc d  \n" +
                "    join (select dw.type_doc ,dw.number_doc, dw.code_wares, sum(dw.quantity) as quantity_input,max(dw.order_doc) as order_doc,sum(quantity_old) as quantity_old from doc_wares dw group by dw.type_doc ,dw.number_doc,code_wares) dw1 on (dw1.number_doc = d.number_doc and d.type_doc=dw1.type_doc)\n" +
                "    join wares w on dw1.code_wares = w.code_wares \n" +
                "    left join DOC_WARES_sample dws on d.number_doc = dws.number_doc and d.type_doc=dws.type_doc and dws.code_wares = w.code_wares\n" +
                "    where d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" +
                " union all\n" + 
                " select dws.order_doc+100000, w.CODE_WARES,w.NAME_WARES,coalesce(dws.quantity,0) as quantity_order,coalesce(dw1.quantity_input,0) as quantity_input, coalesce(dws.quantity_min,0) as quantity_min, coalesce(dws.quantity_max,0) as quantity_max ,coalesce(d.Is_Control,0) as Is_Control, coalesce(dw1.quantity_old,0) as quantity_old\n" +
                "    from Doc d  \n" +
                "    join DOC_WARES_sample dws on d.number_doc = dws.number_doc and d.type_doc=dws.type_doc --and dws.code_wares = w.code_wares\n" +
                "    join wares w on dws.code_wares = w.code_wares \n" +
                "    left join (select dw.type_doc ,dw.number_doc, dw.code_wares, sum(dw.quantity) as quantity_input,sum(dw.quantity_old) as quantity_old from doc_wares dw group by dw.type_doc ,dw.number_doc,code_wares) dw1 on (dw1.number_doc = d.number_doc and d.type_doc=dw1.type_doc and dw1.code_wares = w.code_wares)\n" +
                "    where dw1.type_doc is null and d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" +
                " order by 1";

        if(pTypeResult==2)
            sql="select dw1.order_doc, w.CODE_WARES,w.NAME_WARES,coalesce(dws.quantity,0) as quantity_order,coalesce(dw1.quantity,0) as quantity_input, coalesce(dws.quantity_min,0) as quantity_min, coalesce(dws.quantity_max,0) as quantity_max ,coalesce(d.Is_Control,0) as Is_Control, coalesce(dw1.quantity_old,0) as quantity_old \n" +
                    "    from Doc d  \n" +
                    "    join doc_wares dw1 on (dw1.number_doc = d.number_doc and d.type_doc=dw1.type_doc)\n" +
                    "    join wares w on dw1.code_wares = w.code_wares \n" +
                    "    left join DOC_WARES_sample dws on d.number_doc = dws.number_doc and d.type_doc=dws.type_doc and dws.code_wares = w.code_wares\n"+
                    "    where d.type_doc="+pTypeDoc+" and  d.number_doc = '"+pNumberDoc+"'\n" +
                    " order by 1";



        try {
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    WaresItemModel WaresModel = new WaresItemModel();
                    WaresModel.OrderDoc = mCur.getInt(0);
                    WaresModel.CodeWares = mCur.getInt(1);
                    WaresModel.NameWares = mCur.getString(2);
                    WaresModel.QuantityOrder = mCur.getDouble(3);
                    WaresModel.InputQuantity = mCur.getDouble(4);
                    WaresModel.QuantityMin = mCur.getDouble(5);
                    WaresModel.QuantityMax=(mCur.getInt(7)==1?mCur.getDouble(6):Double.MAX_VALUE);
                    WaresModel.QuantityOld=mCur.getDouble(8);
                    model.add(WaresModel);
                }
            }
        }catch (Exception e){
            e.getMessage();
        }
        return model;
    }

    public WaresItemModel GetScanData(int TypeDoc, String DocNumber,String number) {
        WaresItemModel model = null;
        Cursor mCur;
        String sql;

        Integer intNum = 0;
        boolean isBarCode = true;
        if (number.length() <= 8 && !number.equals("")) {
            intNum = Integer.parseInt(number);
            isBarCode = intNum.toString().length() >= 8;
        }

        if (isBarCode) {
            sql = "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,bc.CODE_UNIT, ud.ABR_UNIT , bc.BAR_CODE  ,w.CODE_UNIT as BASE_CODE_UNIT " +
                    "from BAR_CODE bc " +
                    "join ADDITION_UNIT au on bc.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=bc.CODE_UNIT " +
                    "join wares w on w.CODE_WARES=bc.CODE_WARES " +
                    "join UNIT_DIMENSION ud on bc.CODE_UNIT=ud.CODE_UNIT " +
                    "where bc.BAR_CODE='" + number.trim() + "'";
        } else {
            sql = "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,w.CODE_UNIT, ud.ABR_UNIT , '' as BAR_CODE  ,w.CODE_UNIT as BASE_CODE_UNIT " +
                    "from WARES w " +
                    "join ADDITION_UNIT au on w.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=w.CODE_UNIT " +
                    "join UNIT_DIMENSION ud on w.CODE_UNIT=ud.CODE_UNIT " +
                    "where w.ARTICL='" + number + "'";
        }

        try {
            mCur = mDb.rawQuery(sql, null);
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
            e.getMessage();
        }
        if (model != null) {
            sql = "select coalesce(d.Is_Control,0) as Is_Control, coalesce(quantity_max,0) as quantity_max, coalesce(quantity,0) as quantity from DOC d\n" +
                    " left join DOC_WARES_sample dws on d.Type_doc=dws.Type_doc and d.number_doc=dws.number_doc and dws.code_wares=" + model.CodeWares +
                    " \nwhere  d.Type_doc=" + TypeDoc + " and d.number_doc=\"" + DocNumber + "\"";
            try {
                mCur = mDb.rawQuery(sql, null);
                if (mCur != null && mCur.getCount() > 0) {
                    mCur.moveToFirst();
                    model.QuantityMax=(mCur.getInt(0)==1?mCur.getInt(1):Double.MAX_VALUE);
                    model.QuantityOrder=mCur.getInt(2);
                }
            } catch (Exception e) {
                e.getMessage();
            }

        }

        return model;
    }

    public void UpdateDocState(int pState,int pTypeDoc, String pNumberDoc){
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("state", pState);
            db.update("DOC", values, "Type_doc=" + pTypeDoc+" and number_doc="+pNumberDoc,null);
        }
        catch (Exception e)
        {
            String s=e.getMessage();
        }
    }

    public void SetNullableWares(int parTypeDoc,String parNumberDoc,int CodeWares ){
        long result = -1;
        String s = "";
        try {
            SQLiteDatabase db = mDb;
            ContentValues cv = new ContentValues();
            cv.put("quantity",0);
            mDb.update("DOC_WARES",cv,"code_wares = "+ CodeWares +" and type_doc="+ parTypeDoc+" and number_doc=\""+ parNumberDoc+"\"",null);
        }
        catch (Exception e)
        {
            s=e.getMessage();
        }
    }

    public ArrayList SaveDocWares(int pTypeDoc,String pNumberDoc,int pCodeWares, int pOrderDoc, Double pQuantity ){
        long result = -1;
        String s = "";
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("type_doc", pTypeDoc);
            values.put("number_doc", pNumberDoc);
            values.put("code_wares", pCodeWares);
            values.put("order_doc", pOrderDoc);
            values.put("quantity", pQuantity);
            values.put("quantity_old", 0);
            result = db.insert("DOC_WARES", null, values);
        }
        catch (Exception e)
        {
            s=e.getMessage();
        }

        final boolean status = result != -1;
        final String msg = s;

        return new ArrayList(){{
            add(status);
            add(msg);
        }};
    }


}
