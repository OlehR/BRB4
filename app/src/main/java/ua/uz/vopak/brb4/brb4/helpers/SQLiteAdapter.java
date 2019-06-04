package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.QuantityModel;
import ua.uz.vopak.brb4.brb4.models.RevisionItemModel;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class SQLiteAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;
    GlobalConfig config = GlobalConfig.instance();

    public SQLiteAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public SQLiteAdapter createDatabase() throws SQLException
    {
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

    public SQLiteAdapter open() throws SQLException
    {
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


    public void InsLogPrice(String parBarCode,Integer parIsGood, Integer actionType, Integer packageNumber) {
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("bar_code", parBarCode);
            values.put("is_good", parIsGood);
            values.put("action_type", actionType);
            values.put("package_number", packageNumber);
            db.insert("LogPrice", null, values);
            //db.close();
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
        String sql ="select count(*),sum(case when is_good=0 then 1 else 0 end) from   LogPrice where is_send=0";

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
    public boolean LoadDataDoc(String parSQL)
    {
        int varN=mDb.getVersion();
        try {

           //mDb.execSQL("delete from wares");

            String sql ="select count(*) from   bar_code";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
            }

            String[] varS=parSQL.split(";;;");
            for(int i=0;i<varS.length;i++)
            {

                if(varS[i].trim() != null && !varS[i].trim().isEmpty() && varS[i].length()>10)
                    mDb.execSQL(varS[i]);
            }


            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                mCur.moveToFirst();
                varN = mCur.getInt(0);
            }
            int i=varN++;


        }
        catch (SQLException mSQLException)
        {
            String m=mSQLException.getMessage();
            return false;
         }
        return true;
    }

    public List<ArrayList> GetSendData() {
        int[] varRes = {0,0};
        int varN;
        Cursor mCur;
        List<ArrayList> list = new ArrayList<ArrayList>();
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

            sql ="select bar_code,is_good,DT_insert from LogPrice where is_send=-1";
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                //mCur.moveToFirst() ;
                while (mCur.moveToNext()){
                    ArrayList row = new ArrayList();
                    row.add(mCur.getString(0));
                    row.add(mCur.getInt(1));
                    row.add(mCur.getString(2));
                    row.add(config.NumberPackege);

                    list.add(row);
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
//        String sql = "UPDATE LogPrice SET is_send=1 WHERE is_send=-1";
//        mDb.execSQL(sql);
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

    public List<DocWaresModel> GetDocWares(String number,String DocType) {
        List<DocWaresModel> model = new ArrayList<DocWaresModel>();
        Cursor mCur;
        String sql = "SELECT iw.number_doc,iw.code_wares,iw.order_doc,iw.quantity,iw.quantity_old, w.NAME_WARES FROM DOC_WARES iw LEFT JOIN WARES w ON w.CODE_WARES=iw.code_wares WHERE iw.number_doc = '"+number+"'"+
                "and type_doc="+DocType+
                " order by iw.order_doc asc";

        try {
            //mDb.delete("INVENTORY_WARES", null, null);
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    DocWaresModel WaresModel = new DocWaresModel();
                    WaresModel.Number = mCur.getString(0);
                    WaresModel.CodeWares = mCur.getString(1);
                    WaresModel.OrderDoc = mCur.getString(2);
                    WaresModel.Quantity = mCur.getString(3);
                    WaresModel.OldQuantity = mCur.getString(4);
                    WaresModel.NameWares = mCur.getString(5);
                    model.add(WaresModel);
                }
            }
        }catch (Exception e){
            e.getMessage();
        }


        return model;
    }

    public List<DocWaresModelIncome> GetDocWaresIncome(String number) {
        List<DocWaresModelIncome> model = new ArrayList<DocWaresModelIncome>();
        Cursor mCur;
        String sql = "select w.CODE_WARES,w.NAME_WARES,dws.quantity,dw.quantity from DOC_WARES_sample dws" +
                " join wares w on dws.code_wares = w.code_wares " +
                " left join (select code_wares, sum(quantity) as quantity  from doc_wares dw where dw.number_doc="+ number +" group by code_wares) dw on dws.code_wares = dw.code_wares " +
                " where dws.number_doc="+number +
                " order by dws.order_doc";

        try {
            //mDb.delete("INVENTORY_WARES", null, null);
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    DocWaresModelIncome WaresModel = new DocWaresModelIncome();
                    WaresModel.CodeWares = mCur.getString(0);
                    WaresModel.NameWares = mCur.getString(1);
                    WaresModel.QuantityOrdered = mCur.getFloat(2);
                    WaresModel.QuantityIncoming = mCur.getFloat(3);
                    model.add(WaresModel);
                }
            }
        }catch (Exception e){
            e.getMessage();
        }


        return model;
    }

    public List<DocumentModel> GetDocumentList(String type) {
        String data=config.GetApiJson(150,"\"TypeDoc\":"+type);
        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);
        LoadDataDoc(result);
        List<DocumentModel> model = new ArrayList<DocumentModel>();
        Cursor mCur;
        String sql = "SELECT date_doc,type_doc,number_doc,ext_info,name_user,bar_code,description,dt_insert,state FROM DOC WHERE type_doc = '"+type+"'"+
                     " AND date_doc BETWEEN datetime(CURRENT_TIMESTAMP,'-2 day') AND datetime(CURRENT_TIMESTAMP)";

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

    public void UpdateDocState(String state, String number){
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("state", state);
            db.update("DOC", values, "number_doc="+number,null);
        }
        catch (Exception e)
        {
            String s=e.getMessage();
        }
    }

    public RevisionItemModel GetScanData(String number) {
        RevisionItemModel model = null;
        Cursor mCur;
        String sql;
        Integer intNum = 0;
        boolean isBarCode = true;
        if(number.length() <= 8 && ! number.equals("")) {
            intNum = Integer.parseInt(number);
            isBarCode = intNum.toString().length() >= 8;
        }

        if(isBarCode) {
            sql =   "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,bc.CODE_UNIT, ud.NAME_UNIT , bc.BAR_CODE  ,w.CODE_UNIT as BASE_CODE_UNIT " +
                    "from BAR_CODE bc " +
                    "join ADDITION_UNIT au on bc.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=bc.CODE_UNIT " +
                    "join wares w on w.CODE_WARES=bc.CODE_WARES " +
                    "join UNIT_DIMENSION ud on bc.CODE_UNIT=ud.CODE_UNIT " +
                    "where bc.BAR_CODE='" + number.trim()+"'";
        }else{
            sql =   "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,w.CODE_UNIT, ud.NAME_UNIT , '' as BAR_CODE  ,w.CODE_UNIT as BASE_CODE_UNIT " +
                    "from WARES w " +
                    "join ADDITION_UNIT au on w.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=w.CODE_UNIT " +
                    "join UNIT_DIMENSION ud on w.CODE_UNIT=ud.CODE_UNIT " +
                    "where w.ARTICL='" + number+"'";
        }

        try {
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                mCur.moveToFirst();
                model = new RevisionItemModel();

                model.CodeWares = mCur.getString(0);
                model.NameWares = mCur.getString(1);
                model.Coefficient = mCur.getString(2);
                model.CodeUnit = mCur.getString(3);
                model.NameUnit = mCur.getString(4);
                model.BarCode = mCur.getString(5);
                model.BaseCodeUnit = mCur.getString(6);
            }
        }catch (Exception e){
            e.getMessage();
        }


        return model;
    }

    public QuantityModel GetQuantity(String typeDoc, String numberDoc, String codeWares) {
        QuantityModel model = new QuantityModel();
        Cursor mCur;
        String sql = "select quantity, quantity_min, quantity_max " +
                "from DOC_WARES_sample" +
                " where type_doc='"+typeDoc+"' and number_doc='"+numberDoc+ "' and code_wares='"+codeWares+"'";

        try {
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                mCur.moveToFirst();

                model.Quantity = Integer.parseInt(mCur.getString(0));
                model.QuantityMin = Integer.parseInt(mCur.getString(1));
                model.QuantityMax = Integer.parseInt(mCur.getString(2));
            }
        }catch (Exception e){
            e.getMessage();
        }


        return model;
    }

    public void SetNullableWares(String CodeWares){
        long result = -1;
        String s = "";
        try {
            SQLiteDatabase db = mDb;
            ContentValues cv = new ContentValues();
            cv.put("quantity",0);
            mDb.update("DOC_WARES",cv,"code_wares = "+ CodeWares ,null);
        }
        catch (Exception e)
        {
            s=e.getMessage();
        }
    }

    public List<String> getPrintBlockItemsCount(String packages){
        List<String> data = new ArrayList<String>();
        Cursor mCur;

        String sql = "select count(package_number) from LogPrice WHERE is_good < 0 AND package_number IN("+packages+") GROUP BY package_number";

        mCur = mDb.rawQuery(sql, null);

        if(mCur != null){
            mCur.moveToFirst();
            while (mCur.moveToNext()){
                data.add(mCur.getString(0));
            }
        }

        return data;
    }

    public ArrayList SaveDocWares(String Quantity, String scanOrderDoc, String codeWares, String invNumber, String invTypeDoc){
        long result = -1;
        String s = "";
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("type_doc", invTypeDoc);
            values.put("number_doc", invNumber);
            values.put("code_wares", codeWares);
            values.put("order_doc", scanOrderDoc);
            values.put("quantity", Quantity);
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

    public List<String> getPrintPackageBarcodes(Integer packageNumber, Integer actionType){
        Cursor mCur;
        List<String> data = new ArrayList<String>();

        String sql =   "SELECT bar_code FROM LogPrice WHERE package_number ="+packageNumber+" AND action_type ="+actionType;

        mCur = mDb.rawQuery(sql, null);
        if (mCur!=null && mCur.getCount() > 0) {
            while (mCur.moveToNext()){
                data.add(mCur.getString(0));
            }
        }

        return data;
    }

}
