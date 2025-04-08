package com.example.exercicioaula2android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BancoHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "habitos.db";
    private static final int VERSAO = 2;
    private static final String TABELA = "habitos";

    public BancoHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "categoria TEXT, " +
                "frequencia TEXT, " +
                "feitoHoje INTEGER DEFAULT 0)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABELA);
        onCreate(db);
    }

    public long inserirUsuario(String nome, String categoria, String frequencia) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", nome);
        valores.put("categoria", categoria);
        valores.put("frequencia", frequencia);
        valores.put("feitoHoje", 0);
        return db.insert(TABELA, null, valores);
    }

    public Cursor listarUsuarios() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABELA, null);
    }

    public int atualizarUsuario(int id, String nome, String categoria, String frequencia) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", nome);
        valores.put("categoria", categoria);
        valores.put("frequencia", frequencia);
        return db.update(TABELA, valores, "id = ?", new String[]{String.valueOf(id)});
    }

    public int deletarUsuario(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABELA, "id = ?", new String[]{String.valueOf(id)});
    }

    public void marcarComoFeito(int id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("feitoHoje", 1);
        db.update(TABELA, valores, "id = ?", new String[]{String.valueOf(id)});
    }

    public void resetarFeitoHoje() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("feitoHoje", 0);
        db.update(TABELA, valores, null, null);
    }
}
