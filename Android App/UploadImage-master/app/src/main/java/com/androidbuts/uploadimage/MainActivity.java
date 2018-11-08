package com.androidbuts.uploadimage;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.parser.JSONParser;
import com.androidbuts.permission.PermissionsActivity;
import com.androidbuts.permission.PermissionsChecker;
import com.androidbuts.utils.InternetConnection;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static boolean FirstCheck = false;
    /**
     * Permission List
     */
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    //okHTTP 변수 목록
     Context mContext;
    ImageView imageView;
    TextView textView;
    String imagePath;
    PermissionsChecker checker;
    Toolbar toolbar;


    //백그라운드 Service 변수 목록
    Button button_Start,button_End;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //================= 백그라운드 서비스 코드 ====================================

        //백그라운드 Service Start
        button_Start = (Button)findViewById(R.id.button);
        button_Start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 서비스 시작하기
                Toast.makeText(getApplicationContext(), R.string.string_main_ServiceStartToast, Toast.LENGTH_LONG).show();
                Log.d("test", "액티비티-서비스 시작버튼클릭");
                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        com.androidbuts.Background.MyService.class); // 이동할 컴포넌트
                startService(intent); // 서비스 시작
            }
        });

        button_End= (Button)findViewById(R.id.button2);
        button_End.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 서비스 종료하기
                Toast.makeText(getApplicationContext(), R.string.string_main_ServiceStopToast, Toast.LENGTH_LONG).show();
                Log.d("test", "액티비티-서비스 종료버튼클릭");
                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        com.androidbuts.Background.MyService.class); // 이동할 컴포넌트
                stopService(intent); // 서비스 종료
            }
        });












        //================= okHTTP 소스코드 ====================================
        mContext = getApplicationContext();


        //권한 체크
        checker = new PermissionsChecker(this);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    showImagePopup();
                }
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(imagePath)) {

                    if (InternetConnection.checkConnection(mContext)) {
                       new AsyncTask<Void, Integer, Boolean>() {

                            ProgressDialog progressDialog;

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setMessage(getString(R.string.string_title_upload_progressbar_));
                                progressDialog.show();
                            }

                            @Override
                            protected Boolean doInBackground(Void... params) {

                                try {
                                    JSONObject jsonObject = JSONParser.uploadImage(imagePath);
                                    if (jsonObject != null)
                                        return jsonObject.getString("result").equals("success");

                                } catch (JSONException e) {
                                    Log.i("TAG", "Error : " + e.getLocalizedMessage());
                                }
                                return false;
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                super.onPostExecute(aBoolean);
                                if (progressDialog != null)
                                    progressDialog.dismiss();

                                if (aBoolean)
                                    Toast.makeText(getApplicationContext(), R.string.string_upload_success, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), R.string.string_upload_fail, Toast.LENGTH_LONG).show();

                                imagePath = "";
                                textView.setVisibility(View.VISIBLE);
                                imageView.setVisibility(View.INVISIBLE);
                            }
                        }.execute();
                    } else {
                        Snackbar.make(findViewById(R.id.parentView), R.string.string_internet_connection_warning, Snackbar.LENGTH_INDEFINITE).show();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.parentView), R.string.string_message_to_attach_file, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        });
    }

    /**
     * Showing Image Picker
     */
    private void showImagePopup() {
        // File System.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of file system options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.string_choose_image));
        startActivityForResult(chooserIntent, 1010);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1010) {
            if (data == null) {
                Snackbar.make(findViewById(R.id.parentView), R.string.string_unable_to_pick_image, Snackbar.LENGTH_INDEFINITE).show();
                return;
            }
            Uri selectedImageUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                Log.e("hi"," result :"+columnIndex);
                imagePath = cursor.getString(columnIndex);

                Picasso.with(mContext).load(new File(imagePath))
                        .into(imageView);
                cursor.close();

            } else {
                Snackbar.make(findViewById(R.id.parentView), R.string.string_unable_to_load_image, Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImagePopup();
                    }
                }).show();
            }

            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Toast.makeText(getApplicationContext(), "충북대학교 정통신공학부입니다.", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }
}
