package com.speakforme.joo.speakforme;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private TextToSpeech textToSpeech;
    private SQLiteDatabase banco;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> frases;
    private ArrayList<Integer>ids;


    private EditText texto1;

    //feijao



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = (Button)findViewById(R.id.button);
        editText = (EditText)findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.list);
        criarBanco();
        recuperarFrases();



        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status==TextToSpeech.SUCCESS){
                int resltado = textToSpeech.setLanguage(new Locale("por","POR"));
                if(resltado== TextToSpeech.LANG_MISSING_DATA ||
                        resltado==TextToSpeech.LANG_NOT_SUPPORTED){
                    Log.e("TTS","linguagem não suportada");
                } else{
                    button.setEnabled(true);
                }
            }else{
                Log.e("TTS","falha na inicialização");
            }
            }
        });




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String texto = editText.getText().toString();
                //criarBanco(texto);
                editText.setText("");
                salvarFrases(texto);
                textToSpeech.speak(texto,TextToSpeech.QUEUE_FLUSH,null);


            }
        });





        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Cursor cursor = banco.rawQuery("SELECT * FROM frases WHERE id=" + ids.get(position) + "", null);
                    cursor.moveToFirst();
                    String texto = cursor.getString(cursor.getColumnIndex("frase"));

                    Log.i("TEXTO",texto);
                    textToSpeech.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

    }

    public void criarBanco(){
        try {
            banco = openOrCreateDatabase("banco", MODE_PRIVATE, null);
            banco.execSQL("DROP TABLE frases");
            banco.execSQL("CREATE TABLE IF NOT EXISTS frases(id INTEGER PRIMARY KEY AUTOINCREMENT, frase VARCHAR)");
            //banco.execSQL("INSERT INTO frases (frase) VALUES(" + x + ")");
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void recuperarFrases(){
        try{
            ids= new ArrayList<>();
            frases = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.alist_adapter, R.id.texto1,frases);
            listView.setAdapter(adapter);

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
