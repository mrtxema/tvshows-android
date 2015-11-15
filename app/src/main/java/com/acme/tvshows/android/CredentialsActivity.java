package com.acme.tvshows.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.service.Store;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import java.util.HashMap;
import java.util.Map;

public class CredentialsActivity extends BaseActivity {
    private Store store;
    private Credentials credentials;
    private Map<String, EditText> formFields;
    private TextView txtMessages;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_credentials);
        store = getIntent().getExtras().getParcelable("store");
        credentials = getIntent().getExtras().getParcelable("credentials");

        txtMessages = (TextView) findViewById(R.id.txtMessages);
        TextView credentialsTitle = (TextView) findViewById(R.id.credentialsTitle);
        credentialsTitle.setText(getString(R.string.credentialsTitle, capitalize(store.getCode())));

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                CredentialsActivity.this.finish();
            }
        });

        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new SaveCredentialsTask().execute() ;
            }
        });

        formFields = new HashMap<>();
        LinearLayout lstParameters = (LinearLayout) findViewById(R.id.lstParameters);
        for (String parameter : store.getLoginParameters()) {
            String parameterValue = credentials == null ? null : credentials.getParameters().get(parameter);
            lstParameters.addView(inflateView(parameter, parameterValue));
        }

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    public View inflateView(String item, String value) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.listitem_form, null, false);
        TextView fieldLabel = (TextView) rowView.findViewById(R.id.fieldLabel);
        fieldLabel.setText(capitalize(item));
        EditText formField = (EditText) rowView.findViewById(R.id.fieldInput);
        if (item.toLowerCase().contains("password")) {
            formField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (value != null) {
            formField.setText(value);
        }
        formFields.put(item, formField);
        return rowView;
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private class SaveCredentialsTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;

        private Map<String, String> getFormParameters() {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, EditText> entry : formFields.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getText().toString());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            txtMessages.setText("");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Credentials newCredentials = new Credentials(store.getCode(), getFormParameters());
            try {
                if (credentials != null) {
                    DatabaseManager.getInstance().deleteCredentials(CredentialsActivity.this, credentials);
                }
                DatabaseManager.getInstance().saveCredentials(CredentialsActivity.this, newCredentials);
                return true;
            } catch (StoreException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setResult(RESULT_OK);
                CredentialsActivity.this.finish();
            } else {
                txtMessages.setText(errorMessage);
            }
        }
    }
}
