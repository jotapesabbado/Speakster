package com.speakforme.joo.speakforme;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class Database extends AppCompatActivity {
    private SQLiteDatabase banco;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> frases;
    private ArrayList<Integer>ids;

    public void criarBanco(){
        try {
            banco = SQLiteDatabase.openOrCreateDatabase("banco" , null);
            banco.execSQL("DROP TABLE frases");
            banco.execSQL("CREATE TABLE IF NOT EXISTS frases(id INTEGER PRIMARY KEY AUTOINCREMENT, frase VARCHAR)");
            //banco.execSQL("INSERT INTO frases (frase) VALUES(" + x + ")");
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public ArrayAdapter recuperarFrases(){
        try{
            ids= new ArrayList<>();
            frases = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.alist_adapter, R.id.texto1,frases);
            //listView.setAdapter(adapter);

            Cursor cursor = banco.rawQuery("SELECT * FROM frases ORDER BY id DESC",null);

            cursor.moveToFirst();
            while (cursor!=null) {

                frases.add(cursor.getString(cursor.getColumnIndex("frase")));
                ids.add(cursor.getInt(cursor.getColumnIndex("id")));
                cursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return adapter;
    }





    public void salvarFrases(String texto){
        try{
            if(texto.isEmpty()){
                Toast.makeText(this, "DIGITE UMA FRASE", Toast.LENGTH_SHORT).show();
            }else {
                banco.execSQL("INSERT INTO frases(frase) VALUES ('" + texto + "')");
                recuperarFrases();
                Toast.makeText(this, "FRASE SALVA!", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }




}
