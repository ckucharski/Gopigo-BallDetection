package dpkc.gopigo_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLite extends SQLiteOpenHelper{

    private static final String TABLE_CIRCLE = "table_circle";
    private static final String COL_ID = "ID";
    private static final String COL_NB_CIRCLES = "Nb_circles";

    private static final String CREATE_BDD = "CREATE TABLE " + TABLE_CIRCLE + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NB_CIRCLES + " INTEGER NOT NULL);";

    public SQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL( CREATE_BDD );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}
