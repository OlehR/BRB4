package ua.uz.vopak.brb4.lib.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper
{
    private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME ="brb4.db";// Database name
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
        String sql3;
        if (oldVersion<3 && newVersion >= 3) {

            sql3 = "CREATE TABLE Warehouse (\n" +
                    "    Code       INT  PRIMARY KEY NOT NULL,\n" +
                    "    Number     TEXT,\n" +
                    "    Name       TEXT,\n" +
                    "    Url        TEXT,\n" +
                    "    InternalIP TEXT,\n" +
                    "    ExternalIP TEXT)\n" +
                    "WITHOUT ROWID;";
            db.execSQL(sql3);
        }

        if (oldVersion<4 && newVersion >= 4) {
            sql3="alter table DOC_WARES_sample add COLUMN Name TEXT;";
            db.execSQL(sql3);
            sql3="alter table DOC_WARES_sample add COLUMN BarCode TEXT;";
            db.execSQL(sql3);
            sql3="create index DOC_WARES_sample_BC on DOC_WARES_sample (BarCode);";
            db.execSQL(sql3);
        }
        if (oldVersion<5 && newVersion >= 5) {
            sql3="alter table DOC add COLUMN Color INTEGER;";
            db.execSQL(sql3);
        }
    }

    public DataBaseHelper(Context context)
    {
        super(context, DB_NAME, null, 5);// 1? Its database Version
        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    public void createDataBase() throws IOException
    {
        //If the database does not exist, copy it from the assets.

        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Copy the database from assests
                copyDataBase();
                Utils.WriteLog("e",TAG, "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DB_NAME);
        //dbFile.delete();
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException
    {
        InputStream mInput = mContext.getAssets().open("databases/"+DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        File dbFile = new File(DB_PATH + DB_NAME);
        if(!dbFile.exists())
         dbFile.createNewFile();


        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException
    {
        String mPath = DB_PATH + DB_NAME;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close()
    {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

}