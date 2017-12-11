package dpkc.gopigo_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager {

    private static final int VERSION_BDD = 1;
    private static final String NOM_BDD = "save.db";

    private static final String TABLE_CIRCLE = "table_circle";
    private static final String COL_ID = "ID";
    private static final String COL_NB_CIRCLES = "Nb_circles";

    private SQLiteDatabase bdd;
    private SQLite sqLite;

    public DatabaseManager(Context context)
    {
        sqLite = new SQLite(context, NOM_BDD, null, VERSION_BDD);
    }

    public void open()
    {
        bdd = sqLite.getWritableDatabase();
    }

    public void close()
    {
        bdd.close();
    }

    public void removeDB()
    {
        bdd.delete(TABLE_CIRCLE, null, null);
    }

    public void insertNbCircles(String nbCircles)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NB_CIRCLES, nbCircles);

        bdd.insert(TABLE_CIRCLE, null, contentValues);
    }

    public int getNbDataInDb()
    {
        Cursor c = bdd.rawQuery("SELECT COUNT(*) FROM " + TABLE_CIRCLE, null);
        c.moveToFirst();

        return c.getInt(0);
    }

    public String getNbCirclesWithIndex(int index)
    {
        Cursor c = bdd.query(TABLE_CIRCLE, new String[]{COL_NB_CIRCLES}, COL_ID + " LIKE \"" + String.valueOf(index) + "\"", null, null, null, null);
        c.moveToFirst();

        return c.getString(0);
    }
}
