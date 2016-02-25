package at.ac.tgm.hit.dezsyslabor.hampl.dezsyslabor_11;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


/**
 * A login screen that offers login via email/password and registration via email/name/password
 *
 * @author Burkhard Hampl [bhampl@student.tgm.ac.at]
 * @version 1.0
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login and registration task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserRegistrationTask mRegTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mNameView;
    private Button mEmailRegisterButton;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login and registration form
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mNameView = (EditText) findViewById(R.id.name);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mEmailRegisterButton = (Button) findViewById(R.id.email_register);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mTextView = (TextView) findViewById(R.id.textView);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // register button only enabled if name is given
        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mEmailRegisterButton.setEnabled(false);
                } else {
                    mEmailRegisterButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one
        if (TextUtils.isEmpty(password)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address, if the user entered one
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual registration attempt is made.
     */
    private void attemptRegister() {
        if (mRegTask != null) {
            return;
        }

        // Reset errors
        mEmailView.setError(null);
        mNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the registration attempt
        String email = mEmailView.getText().toString();
        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one
        if (TextUtils.isEmpty(password)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check the user entered a name
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        // Check for a valid email address, if the user entered one
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registration attempt
            showProgress(true);
            mRegTask = new UserRegistrationTask(email, name, password);
            mRegTask.execute((Void) null);
        }
    }

    /**
     * Checks if the given email address is valid
     *
     * @param email the given email to check
     * @return if the email contains valid
     * @see android.util.Patterns
     */
    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Checks if the given password is valid
     *
     * @param password the given password to check
     * @return if the password is valid
     */
    private boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password) && password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form
     *
     * @param show if the progress UI should be shown
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user
     *
     * @author Burkhard Hampl [bhampl@student.tgm.ac.at]
     * @version 1.0
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String message;
        private boolean fail;

        /**
         * The default constructor
         *
         * @param email    the email
         * @param password the password
         */
        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            fail = false;
            message = getString(R.string.message_unknown_error);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Login
            SyncHttpClient client = new SyncHttpClient();
            client.setTimeout(45000);
            JSONObject jsonObject = new JSONObject();
            StringEntity entity;
            try {
                jsonObject.put("email", this.mEmail);
                jsonObject.put("password", this.mPassword);
                entity = new StringEntity(jsonObject.toString());
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            client.post(getBaseContext(), "http://dezsys-09.herokuapp.com/login", entity, RequestParams.APPLICATION_JSON, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        // If the response is JSONObject
                        if (statusCode != 200) fail = true;
                        message = response.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    try {
                        // If something went wrong
                        fail = true;
                        if (errorResponse != null) {
                            message = errorResponse.getString("message");
                        } else {
                            message = getString(R.string.message_unknown_error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success && !fail) {
                mTextView.setTextColor(Color.BLACK);
            } else {
                mTextView.setTextColor(Color.RED);
            }
            mTextView.setText(message);

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous registration task used to authenticate
     * the user
     *
     * @author Burkhard Hampl [bhampl@student.tgm.ac.at]
     * @version 1.0
     */
    public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mName;
        private final String mPassword;
        private String message;
        private boolean fail;

        /**
         * The default constructor
         *
         * @param email    the email
         * @param name     the name
         * @param password the password
         */
        UserRegistrationTask(String email, String name, String password) {
            mEmail = email;
            mName = name;
            mPassword = password;
            fail = false;
            message = getString(R.string.message_unknown_error);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Login
            SyncHttpClient client = new SyncHttpClient();
            client.setTimeout(45000);
            JSONObject jsonObject = new JSONObject();
            StringEntity entity;
            try {
                jsonObject.put("email", this.mEmail);
                jsonObject.put("name", this.mName);
                jsonObject.put("password", this.mPassword);
                entity = new StringEntity(jsonObject.toString());
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            client.post(getBaseContext(), "http://dezsys-09.herokuapp.com/register", entity, RequestParams.APPLICATION_JSON, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        // If the response is JSONObject
                        if (statusCode != 201) fail = true;
                        message = response.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    try {
                        // If something went wrong
                        fail = true;
                        if (errorResponse != null) {
                            message = errorResponse.getString("message");
                        } else {
                            message = getString(R.string.message_unknown_error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegTask = null;
            showProgress(false);

            if (success && !fail) {
                mTextView.setTextColor(Color.BLACK);
            } else {
                mTextView.setTextColor(Color.RED);

            }
            mTextView.setText(message);

        }

        @Override
        protected void onCancelled() {
            mRegTask = null;
            showProgress(false);
        }
    }

}

