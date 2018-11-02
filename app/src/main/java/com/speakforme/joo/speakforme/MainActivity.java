package com.speakforme.joo.speakforme;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {



    private EditText editText;
    private ImageButton botaoFala;
    private ImageButton botaoAdd;
    private ImageButton botaoBack;
    private ImageButton listconf;
  //  private ImageButton botaoConf;
    //private Button button;
    private TextToSpeech textToSpeech;
    private TextToSpeech textToSpeech2;
    private SQLiteDatabase banco;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> frases;
    private ArrayList<Integer>ids;
    private String texto;
    private Locale local;
    private Intent intent;
    private int RESULTADO_OK = 1;
    private Dialog dialogo;

    private boolean CONFIGURACOES_ALTERADAS =false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.button_conf:
                intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent,RESULTADO_OK);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(CONFIGURACOES_ALTERADAS && hasFocus){
            Log.e("RES","/batata");

            this.recreate();

            CONFIGURACOES_ALTERADAS=false;
           // this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RESULTADO_OK){
            CONFIGURACOES_ALTERADAS = true;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LOGO DA ACTION BAR
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);



        //DIALOGO
        dialogo = new Dialog(this);


       // button = (Button)findViewById(R.id.button);

        botaoFala = (ImageButton)findViewById(R.id.buttonFala);
        botaoAdd = (ImageButton)findViewById(R.id.buttonAdd);
        botaoBack = (ImageButton)findViewById(R.id.buttonBack);
      //  botaoConf = (ImageButton) findViewById(R.id.conf);
        listconf = (ImageButton)findViewById(R.id.listconf);

        editText = (EditText)findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.list);
        criarBanco();
        recuperarFrases();


   /*    botaoConf.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //startActivity(new Intent(Service.));

               intent = new Intent();
               intent.setAction("com.android.settings.TTS_SETTINGS");
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivityForResult(intent,RESULTADO_OK);
           }
       });

*/





        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status==TextToSpeech.SUCCESS){

                int resltado = textToSpeech.setLanguage(verificaLocal());
                if(resltado== TextToSpeech.LANG_MISSING_DATA ||
                        resltado==TextToSpeech.LANG_NOT_SUPPORTED){
                    Log.e("TTS","linguagem não suportada");
                } else{
                    botaoFala.setEnabled(true);
                }
            }else{
                Log.e("TTS","falha na inicialização");
            }
            }
        });


        botaoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texto = editText.getText().toString();
                salvarFrases(texto);
                editText.setText("");


            }
        });

        botaoFala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texto = editText.getText().toString();
                //criarBanco(texto);
                editText.setText("");
                textToSpeech.speak(texto,TextToSpeech.QUEUE_FLUSH,null);

            }
        });

        botaoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(texto);
            }
        });

       /* listconf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup(v);
            }
        });*/


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






    public Locale verificaLocal(){

        local.getDefault();
        return local;
    }
    public void criarBanco(){
        try {

            banco = openOrCreateDatabase("banco", 0x0000, null);

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
            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.list_adapter, R.id.texto1,frases);
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
        try {
            if (texto.isEmpty()) {
                Toast.makeText(this, "DIGITE UMA FRASE", Toast.LENGTH_SHORT).show();
            } else {
                banco.execSQL("INSERT INTO frases(frase) VALUES ('" + texto + "')");
                recuperarFrases();
                Toast.makeText(this, "FRASE SALVA!", Toast.LENGTH_SHORT).show();
            }


        }catch (Exception e){
            e.getMessage().toString();
            Log.e("CLICOU",e.getMessage().toString());
        }

    }


    public void popup(View view){
        dialogo.setContentView(R.layout.tela_alarme);
        /*txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("M");
        btnFollow = (Button) myDialog.findViewById(R.id.btnfollow);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        }); */
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.show();
    }



    }