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

import ua.uz.vopak.brb4.brb4.models.InventoryModel;
import ua.uz.vopak.brb4.brb4.models.RevisionItemModel;

public class SQLiteAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

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


    public void InsLogPrice(String parBarCode,Integer parIsGood) {
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("bar_code", parBarCode);
            values.put("is_good", parIsGood);
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
    public boolean LoadDataInventory(String parSQL)
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
                    sql = "UPDATE LogPrice SET is_send=-1 WHERE `rowid` IN (SELECT `rowid` FROM LogPrice WHERE is_send=0 LIMIT 100)";
                    mDb.execSQL(sql);
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
        String sql = "UPDATE LogPrice SET is_send=1 WHERE is_send=-1";
        mDb.execSQL(sql);
    }

    public void AddConfigPair(String name, String value) {
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("NAME_VAR", name);
            values.put("DATA_VAR", value);
            db.insert("CONFIG", null, values);
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

    public List<InventoryModel> GetInventories(String number) {
        List<InventoryModel> model = new ArrayList<InventoryModel>();
        Cursor mCur;
        String sql = "SELECT iw.number_inventory,iw.code_wares,iw.nn,iw.quantity,iw.quantity_old, w.NAME_WARES FROM INVENTORY_WARES iw LEFT JOIN WARES w ON w.CODE_WARES=iw.code_wares WHERE number_inventory = '"+number+"'"+
                "order by iw.nn asc";

        try {
            //mDb.delete("INVENTORY_WARES", null, null);
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                while (mCur.moveToNext()){
                    InventoryModel inventory = new InventoryModel();
                    inventory.Number = mCur.getString(0);
                    inventory.CodeWares = mCur.getString(1);
                    inventory.NN = mCur.getString(2);
                    inventory.Quantity = mCur.getString(3);
                    inventory.OldQuantity = mCur.getString(4);
                    inventory.NameWares = mCur.getString(5);
                    model.add(inventory);
                }
            }
        }catch (Exception e){
            e.getMessage();
        }


        return model;
    }

    public RevisionItemModel GetRevisionScanData(String number) {
        RevisionItemModel model = new RevisionItemModel();
        Cursor mCur;
        String sql = "select w.CODE_WARES,w.NAME_WARES,au.COEFFICIENT,bc.CODE_UNIT, ud.NAME_UNIT , bc.BAR_CODE  from BAR_CODE bc " +
                "join ADDITION_UNIT au on bc.CODE_WARES=au.CODE_WARES and au.CODE_UNIT=bc.CODE_UNIT " +
                "join wares w on w.CODE_WARES=bc.CODE_WARES " +
                "join UNIT_DIMENSION ud on bc.CODE_UNIT=ud.CODE_UNIT " +
                "where bc.BAR_CODE="+number;

        try {
            mCur = mDb.rawQuery(sql, null);
            if (mCur!=null && mCur.getCount() > 0) {
                mCur.moveToFirst();

                model.CodeWares = mCur.getString(0);
                model.NameWares = mCur.getString(1);
                model.Coefficient = mCur.getString(2);
                model.CodeUnit = mCur.getString(3);
                model.NameUnit = mCur.getString(4);
                model.BarCode = mCur.getString(5);
            }
        }catch (Exception e){
            e.getMessage();
        }


        return model;
    }

    public ArrayList SaveRevisionData(String count, String scanNN, String codeWares, String invNumber){
        long result = -1;
        String s = "";
        try {
            SQLiteDatabase db = mDb;
            ContentValues values = new ContentValues();
            values.put("number_inventory", invNumber);
            values.put("code_wares", codeWares);
            values.put("nn", scanNN);
            values.put("quantity", count);
            values.put("quantity_old", 0);
            result = db.insert("INVENTORY_WARES", null, values);
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
