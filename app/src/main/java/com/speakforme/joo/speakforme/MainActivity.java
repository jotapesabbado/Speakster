package com.speakforme.joo.speakforme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText editText;
    private ImageButton botaoFala;
    private ImageButton botaoAdd;
    private ImageButton botaoBack;
  //  private ImageButton botaoConf;
    //private Button button;
    private TextToSpeech textToSpeech;
    private SQLiteDatabase banco;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> frases;
    private ArrayList<Integer>ids;
    private String texto;
    private Locale local;
    private Intent intent;
    private int RESULTADO_OK = 1;
  //  private Dialog dialogo;
  //  private Dialog confirma_exclusao;
    TextView teste;
    public static final int RequestPermissionCode = 1;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;


    private boolean CONFIGURACOES_ALTERADAS =false;
/*
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
*/
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
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher_foreground);

        teste=(TextView)findViewById(R.id.teste);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //DIALOGO
     //   dialogo = new Dialog(this);


       // button = (Button)findViewById(R.id.button);

        botaoFala = (ImageButton)findViewById(R.id.buttonFala);
        botaoAdd = (ImageButton)findViewById(R.id.buttonAdd);
        botaoBack = (ImageButton)findViewById(R.id.buttonBack);
      //  botaoConf = (ImageButton) findViewById(R.id.conf);

        editText = (EditText)findViewById(R.id.editText);
        listView = (ListView)findViewById(R.id.list);
        ids= new ArrayList<>();
        frases = new ArrayList<String>();
     //   confirma_exclusao = new Dialog(this);


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
                closeKeyboard();
            }
        });

        botaoFala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texto = editText.getText().toString();
                //criarBanco(texto);
                editText.setText("");

                //recarega posição do gps
                atualizaLocal();


                textToSpeech.speak(texto,TextToSpeech.QUEUE_FLUSH,null);

            }
        });

        botaoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(texto);
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    atualizaLocal();
                    int idAtual = ids.get(position);
                    Cursor cursor = banco.rawQuery("SELECT * FROM frases WHERE id=" + idAtual + "", null);
                    cursor.moveToFirst();
                    banco.execSQL("INSERT INTO `ocorrencia`(`id_frase`, `latitude`, `longitude`, `dia_hora`) VALUES (" + idAtual + ","+ lastLocation.getLatitude() +","+ lastLocation.getLongitude() +",dateTIME(strftime('%s', 'now'),'unixepoch','localtime'));");
                    String texto = cursor.getString(cursor.getColumnIndex("frase"));
                    textToSpeech.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
                    recuperarFrases();
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
            banco.execSQL("drop table frases");
            banco.execSQL("drop table ocorrencia");
            banco.execSQL("drop table pontuacao");

            banco.execSQL("create table if not exists frases (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, frase VARCHAR(1000) NOT NULL);");
            banco.execSQL("create table if not exists ocorrencia (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,id_frase INTEGER NOT NULL, latitude DOUBLE NOT NULL DEFAULT 0, longitude DOUBLE NOT NULL DEFAULT 0, dia_hora TIMESTAMP NOT NULL,FOREIGN KEY (id_frase) REFERENCES frases(id) ON DELETE CASCADE);");
            banco.execSQL("create table if not exists pontuacao (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, id_frase INTEGER NOT NULL, frase VARCHAR(1000) NOT NULL, pontos INTEGER NOT NULL DEFAULT 0, CONSTRAINT fk_id_frase FOREIGN KEY (id_frase) REFERENCES frases(id) ON DELETE CASCADE);");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public double calcMedia(ArrayList<Double> unix){
        double totalSoma = 0;
        for(Double valor : unix) {
            totalSoma += valor;
        }
        return totalSoma / unix.size();
    }
    public double calcDesvioPadrao(ArrayList<Double> unix, String type){
        double potencia = 0;
        double totalPotencia = 0;
        double dp = 0;
        double media = calcMedia(unix);
        double valorFinal = 0;

        for(Double valor : unix) {
            potencia = Math.pow(valor - media,2);
            totalPotencia += potencia / unix.size();
        }

        dp = Math.sqrt(totalPotencia);
        switch(type) {
            case "DP":
                valorFinal = dp;
            case "MA":
                valorFinal = media;
            case "INICIO":
                valorFinal = media - dp;
            case "FIM":
                valorFinal = media + dp;

        }
    return valorFinal;
    }


    private void recuperarFrases(){
        try{
            adapter = new MyListAdapter(this,R.layout.list_adapter,frases);
            listView.setAdapter(adapter);
            //adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.list_adapter, R.id.texto1,frases);
            //listView.setAdapter(new MyListAdapter(this,R.layout.list_adapter,frases));
            banco.execSQL("drop table if exists intervalo_dp;");
            banco.execSQL("drop table if exists ocorrencia_id;");
            banco.execSQL("drop table if exists pontos_hora;");
            banco.execSQL("drop table if exists pontos_hora_a;");
            banco.execSQL("drop table if exists distancia_pontos;");
            banco.execSQL("drop table if exists intervalo;");

            banco.execSQL("CREATE TABLE intervalo AS SELECT id_frase, strftime('%H:%M:%S',dia_hora)AS HORA , CAST(strftime('%s',dia_hora)AS INTEGER) AS UNIX, CAST(strftime('%w',dia_hora)AS INTEGER) AS DIA FROM ocorrencia GROUP BY id HAVING UNIX BETWEEN ((strftime('%s', 'now') - 10800) - 7200) AND ((strftime('%s', 'now') - 10800) + 7200) AND DIA=strftime('%w',datetime(strftime('%s', 'now') - 10800,'unixepoch'));");

//            Cursor tabelaOcorencia = banco.rawQuery("SELECT * FROM intervalo",null);
//            tabelaOcorencia.moveToFirst();
//            while (tabelaOcorencia!=null){
//                Log.e("id_frase",":"+tabelaOcorencia.getInt(tabelaOcorencia.getColumnIndex("id_frase")));
//                Log.e("HORA",":"+tabelaOcorencia.getDouble(tabelaOcorencia.getColumnIndex("HORA")));
//                Log.e("UNIX",":"+tabelaOcorencia.getDouble(tabelaOcorencia.getColumnIndex("UNIX")));
//                Log.e("DIA",":"+tabelaOcorencia.getInt(tabelaOcorencia.getColumnIndex("DIA")));
//                tabelaOcorencia.moveToNext();
//            }
            banco.execSQL("create table if not exists intervalo_dp (DP DOUBLE NOT NULL, MA TIMESTAMP NOT NULL,FIM TIMESTAMP NOT NULL, INICIO TIMESTAMP NOT NULL )");
            Cursor cursor_intervalo = banco.rawQuery("SELECT UNIX FROM intervalo",null);
            cursor_intervalo.moveToFirst();
            ArrayList<Double> unix = new ArrayList<>();
            try {
                while (cursor_intervalo != null) {
                    unix.add(cursor_intervalo.getDouble(cursor_intervalo.getColumnIndex("UNIX")));
                    cursor_intervalo.moveToNext();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            banco.execSQL("INSERT INTO intervalo_dp(DP, MA, INICIO, FIM) VALUES ("+ calcDesvioPadrao(unix,"DP")+",DATETIME(" + calcDesvioPadrao(unix,"MA") + ", 'unixepoch', 'localtime'),DATETIME(" + calcDesvioPadrao(unix,"INICIO") + ", 'unixepoch', 'localtime'),DATETIME(" + calcDesvioPadrao(unix,"FIM") + ", 'unixepoch', 'localtime'))");

            banco.execSQL("CREATE TABLE if not exists ocorrencia_id (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, maior_id INTEGER NOT NULL);");
            banco.execSQL("INSERT INTO ocorrencia_id(maior_id) SELECT id as maior_id FROM ocorrencia group by id_frase having count(*) = (SELECT count(id_frase) as maior FROM ocorrencia GROUP BY id_frase order by maior desc limit 1);");

            banco.execSQL("CREATE TABLE if not exists pontos_hora (pontos INTEGER DEFAULT 10, HORA NOT NULL, id_frase INTEGER NOT NULL);");
            banco.execSQL("INSERT INTO pontos_hora(HORA, id_frase) SELECT intervalo.HORA, intervalo.id_frase FROM intervalo,intervalo_dp WHERE intervalo.UNIX BETWEEN strftime('%s',intervalo_dp.INICIO) AND strftime('%s',intervalo_dp.FIM)");

            banco.execSQL("CREATE TABLE if not exists pontos_hora_a AS SELECT intervalo.HORA, intervalo.id_frase FROM intervalo,intervalo_dp WHERE intervalo.UNIX BETWEEN (strftime('%s' ,intervalo_dp.INICIO) + (intervalo_dp.DP / 2)) AND (strftime('%s' ,intervalo_dp.FIM) - (intervalo_dp.DP / 2)) group by intervalo.id_frase");
            banco.execSQL("ALTER TABLE pontos_hora_a ADD pontos INTEGER DEFAULT 40;");

            banco.execSQL("CREATE TABLE if not exists distancia_pontos (id_frase INTEGER NOT NULL, DISTANCIA double NOT NULL);");
            atualizaLocal();
            Cursor cursor_ocorencia = banco.rawQuery("SELECT id_frase, latitude, longitude FROM ocorrencia",null);
            cursor_ocorencia.moveToFirst();
            try {
                while (cursor_ocorencia != null) {
                    int id = cursor_ocorencia.getInt(cursor_ocorencia.getColumnIndex("id_frase"));
                    double latitude = cursor_ocorencia.getDouble(cursor_ocorencia.getColumnIndex("latitude"));
                    double longitude = cursor_ocorencia.getDouble(cursor_ocorencia.getColumnIndex("longitude"));
                    banco.execSQL("INSERT INTO distancia_pontos(id_frase, DISTANCIA) VALUES (" + id + "," + calculoDistancia(latitude, longitude, lastLocation.getLatitude(), lastLocation.getLongitude()) + ");");
                    cursor_ocorencia.moveToNext();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            banco.execSQL("INSERT INTO pontuacao(id_frase, frase) select id, frase from frases;");

            banco.execSQL("UPDATE pontuacao SET pontos=pontos + 30 where EXISTS (SELECT id_frase FROM distancia_pontos WHERE id_frase = pontuacao.id_frase and distancia_pontos.DISTANCIA <= 1)");

            banco.execSQL("UPDATE pontuacao SET pontos=pontos + 9 where EXISTS (SELECT pontos FROM ocorrencia_id WHERE maior_id = pontuacao.id_frase)");

            banco.execSQL("UPDATE pontos_hora SET pontos=pontos + (select pontos from pontos_hora_a) where EXISTS (SELECT pontos FROM pontos_hora_a WHERE id_frase = pontos_hora.id_frase)");

            banco.execSQL("UPDATE pontuacao SET pontos=pontos + (select pontos from pontos_hora  WHERE id_frase = pontuacao.id_frase) where EXISTS (SELECT pontos FROM pontos_hora WHERE id_frase = pontuacao.id_frase)");

            frases.clear();
            ids.clear();
            Cursor cursor = banco.rawQuery("SELECT * FROM pontuacao ORDER BY pontos DESC",null);

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
                //independente se tem ou não a distancia precisa ser atualizada pra ser adicionada na tabela de ocorrencias
                atualizaLocal();
                int id = 0;
                try {
                    Cursor cursor = banco.rawQuery("SELECT id FROM frases where frase = '" + texto + "'", null);
                    cursor.moveToFirst();
                    id = cursor.getInt(cursor.getColumnIndex("id"));
                }catch (Exception e){
                    Log.e("Erro id", e.toString());
                }
                // se tem a frase igual a essa e se não tem
                //tem: pega o id e adiciona em ocorencias
                if(id != 0){
                    Toast.makeText(this, "A frase '" + texto + " já consta na lista", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        banco.execSQL("INSERT INTO frases(frase) VALUES ('" + texto + "');");
                        banco.execSQL("INSERT INTO ocorrencia(id_frase, latitude, longitude, dia_hora) VALUES (" + idUltimaOcorencia() + "," + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + ",dateTIME(strftime('%s', 'now'),'unixepoch','localtime'));");
                        recuperarFrases();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Toast.makeText(this, "FRASE SALVA!", Toast.LENGTH_SHORT).show();
                }
            }


        }catch (Exception e){
            e.getMessage().toString();
            Log.e("CLICOU",e.getMessage().toString());
        }

    }


    public int idUltimaOcorencia(){
        int id = 1;
        try {
            Cursor cursor1 = banco.rawQuery("SELECT MAX(id) AS id FROM frases", null);
            cursor1.moveToFirst();
            id = cursor1.getInt(cursor1.getColumnIndex("id"));
        }catch (Exception e){
            Log.e("ultimo id", e.toString());
        }
        return id;
    }



    /*  public void popup(View view){
        dialogo.setContentView(R.layout.tela_alarme);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("M");
        btnFollow = (Button) myDialog.findViewById(R.id.btnfollow);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.show();
    }*/

    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        public MyListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder mainViewHolder = null;

            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.delete_lista = (ImageButton)convertView.findViewById(R.id.delete_lista);
                viewHolder.texto_lista = (TextView)convertView.findViewById(R.id.texto_lista);


                viewHolder.texto_lista.setText(frases.get(position));


                viewHolder.delete_lista.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confima_Exclusao(position);
                        //banco.execSQL("delete from frases where frase=('" + frases.get(position) + "')");
                        //adapter.clear();
                        //recuperarFrases();
                    }
                });


                convertView.setTag(viewHolder);


            }else {
                mainViewHolder = (ViewHolder)convertView.getTag();
                mainViewHolder.texto_lista.setText(getItem(position));

            }

            return convertView;
        }
    }

    public class ViewHolder{
        TextView texto_lista;
        ImageButton delete_lista;
    }

    public void closeKeyboard() {

        View vies = this.getCurrentFocus();
        if (vies != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(vies.getWindowToken(), 0);
        }
    }

    public void confima_Exclusao(final int posicao) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Excluir");
        builder.setMessage("Deseja realmente excluir a frase:"+frases.get(posicao));
        builder.setPositiveButton("Confirma",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        banco.execSQL("delete from frases where frase=('" + frases.get(posicao) + "')");
                        adapter.clear();
                        recuperarFrases();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void navConf(View view){
        intent = new Intent();
        intent.setAction("com.android.settings.TTS_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent,RESULTADO_OK);
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                lastLocation = location;
                                getLocation(location);

                            }
                        }
                    });
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{ACCESS_FINE_LOCATION}, RequestPermissionCode);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("MainActivity", "Connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("MainActivity", "Connection suspendedd");
    }

    public void getLocation(Location location){
        teste.setText(String.valueOf(location.getLatitude())+""+String.valueOf(location.getLongitude()));


    }

    public void atualizaLocal(){
        fusedLocationProviderClient.flushLocations();
        if(googleApiClient.isConnected())googleApiClient.disconnect();
        googleApiClient.connect();
        Toast.makeText(getApplicationContext(), "teste2", Toast.LENGTH_SHORT).show();

    }

    public double calculoDistancia(double lat1, double lon1, double lat2, double lon2) {
        double R = 6372.8; // In kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = Math.asin(Math.sqrt(a));
        return 2*R * c;
    }

}