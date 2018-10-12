package ua.uz.vopak.brb4.brb4;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
}
