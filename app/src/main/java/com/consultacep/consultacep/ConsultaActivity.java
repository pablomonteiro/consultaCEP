package com.consultacep.consultacep;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.consultacep.consultacep.webconnection.ClientConnection;
import com.consultacep.consultacep.webconnection.model.Endereco;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

public class ConsultaActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ConsultaActivity.class.getSimpleName();
    private static final String MSG_EXCEPTION = "Unable to retrieve data. URL may be invalid.";
    private static final String MSG_ERROR_CONNECT_FAIL = "Não foi possível conectar ao servidor!";
    private static final String SITE_CONSULTA_CEP = "http://cep.republicavirtual.com.br/web_cep.php?cep=";
    private static final String MSG_ERROR_INVALID_CEP = "CEP Inválido!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);
        Button btn = (Button) findViewById(R.id.pesquisa);
        btn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_consulta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        String url = montarURL(((TextView) findViewById(R.id.codigoDoCep)).getText().toString());
        new DownloadTask().execute(url);
    }

    private String montarURL(String cep){
        return SITE_CONSULTA_CEP.concat(cep).concat("&formato=json");
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //do your request in here so that you don't interrupt the UI thread
            try {
                return downloadContent(params[0]);
            } catch (IOException e) {
                return MSG_EXCEPTION;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(MSG_EXCEPTION.equals(result)) {
                Toast.makeText(ConsultaActivity.this, MSG_ERROR_CONNECT_FAIL, Toast.LENGTH_LONG).show();
            } else {
                Endereco endereco = Endereco.fromJson(result);
                imprimeResposta(endereco);
            }
        }
    }

    private void imprimeResposta(Endereco endereco) {
        if(endereco != null) {
            ((TextView) findViewById(R.id.uf)).setText(endereco.getUf());
            ((TextView) findViewById(R.id.cidade)).setText(endereco.getCidade());
            ((TextView) findViewById(R.id.bairro)).setText(endereco.getBairro());
            ((TextView) findViewById(R.id.logradouro)).setText(endereco.getLogradouro());
        } else {
            Toast.makeText(ConsultaActivity.this, MSG_ERROR_INVALID_CEP, Toast.LENGTH_LONG).show();
        }
    }

    private String downloadContent(String myurl) throws IOException {
        InputStream is = null;
        int length = 500;

        try {
            HttpURLConnection conn = ClientConnection.createConnection(myurl);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            String contentAsString = convertInputStreamToString(is, length);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String convertInputStreamToString(InputStream stream, int length) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[length];
        reader.read(buffer);
        return new String(buffer);
    }
}
