package com.dzone.kpra_app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dzone.kpra_app.JsonParser.Check_internet_connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.security.auth.login.LoginException;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton camera;
    Button submit;
    EditText tax, bill, invoice;
    ImageView image;
    Bitmap myBitmap;
    CoordinatorLayout constraint_layout;

    String Stax, Sbill, ImageName = "image_name", ImagePath = "image_path", userChoosenTask,
            user_id, Sinvoice, name;
    int SELECT_FILE = 1, REQUEST_CAMERA = 0;
    boolean check = true;
    Uri picUri;
    SharedPreferences preferences;
    long time;
    String currentDateandTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        invoice = findViewById(R.id.invoice);
        camera = findViewById(R.id.camera);
        submit = findViewById(R.id.button2);
        tax = findViewById(R.id.tax2);
        bill = findViewById(R.id.amount);
        image = findViewById(R.id.imageView2);
        constraint_layout = findViewById(R.id.constraint_layout);


        preferences = this.getSharedPreferences("DataStore", Context.MODE_PRIVATE);
        user_id = preferences.getString("user_id", "Mr");
        name = preferences.getString("name", "Mr");


        //ask all permissions
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                time = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy-hhmm");
                currentDateandTime = sdf.format(new Date());

                Stax = tax.getText().toString();
                Sbill = bill.getText().toString();
                Sinvoice = invoice.getText().toString();

                if (Sbill.equals("") && new Check_internet_connection(getApplicationContext()).isNetworkAvailable()) {
                    bill.setError("Enter Bill Amount");
                    bill.requestFocus();
                } else if (pictureFilePath.equals("") && myBitmap == null) {
                    Snackbar snackbar = Snackbar
                            .make(constraint_layout, "Select an Image", Snackbar.LENGTH_LONG);
                    snackbar.show();

                } else {

                    if (new Check_internet_connection(getApplicationContext()).isNetworkAvailable()) {


                        ImageUploadToServerFunction();
                        //saveImage_Function();

                    }
                    else {

                        dialog();

                        Snackbar snackbar = Snackbar
                                .make(constraint_layout, "No Internet Connectivity", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }

            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


    }


    ProgressDialog progressDialog;

    //image ulpoading
    public void ImageUploadToServerFunction() {

//        ByteArrayOutputStream byteArrayOutputStreamObject;
//
//        byteArrayOutputStreamObject = new ByteArrayOutputStream();
//
//        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
//
//        byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
//
//        final String ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        image.buildDrawingCache();
        myBitmap = image.getDrawingCache();

        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image_=stream.toByteArray();
        Log.e("ByteImageArray","byte array:"+image_);

        final String ConvertImage = Base64.encodeToString(image_, 0);


        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();

                progressDialog = ProgressDialog.show(MainActivity.this, "Uploading", "Please Wait..", false, false);
            }

            @Override
            protected void onPostExecute(String string1) {

                super.onPostExecute(string1);

                // Dismiss the progress dialog after done uploading.
                progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(MainActivity.this, string1, Toast.LENGTH_LONG).show();
                if (string1.equals("Data Uploaded.")) {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }

                // Setting image as transparent after done uploading.
//                imageView.setImageResource(android.R.color.transparent);


            }

            @Override
            protected String doInBackground(Void... params) {

                ImageProcessClass imageProcessClass = new ImageProcessClass();

                HashMap<String, String> HashMapParams = new HashMap<String, String>();

                HashMapParams.put(ImageName, "image");
                HashMapParams.put("user_id", user_id);
                HashMapParams.put("name", name);
                HashMapParams.put("invoice", Sinvoice);
                HashMapParams.put("tax_amount", Stax);
                HashMapParams.put("milliseconds", time + "");
                HashMapParams.put("time", currentDateandTime);
                HashMapParams.put("total_bill", Sbill);
                HashMapParams.put(ImagePath, ConvertImage);

                String FinalData = imageProcessClass.ImageHttpRequest(Url.ServerUploadPath, HashMapParams);

                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();

        AsyncTaskUploadClassOBJ.execute();
    }

    public class ImageProcessClass {

        public String ImageHttpRequest(String requestURL, HashMap<String, String> PData) {

            StringBuilder stringBuilder = new StringBuilder();

            try {

                URL url;
                HttpURLConnection httpURLConnectionObject;
                OutputStream OutPutStream;
                BufferedWriter bufferedWriterObject;
                BufferedReader bufferedReaderObject;
                int RC;

                url = new URL(requestURL);

                httpURLConnectionObject = (HttpURLConnection) url.openConnection();

                httpURLConnectionObject.setReadTimeout(19000);

                httpURLConnectionObject.setConnectTimeout(99000);

                httpURLConnectionObject.setRequestMethod("POST");

                httpURLConnectionObject.setDoInput(true);

                httpURLConnectionObject.setDoOutput(true);

                OutPutStream = httpURLConnectionObject.getOutputStream();

                bufferedWriterObject = new BufferedWriter(

                        new OutputStreamWriter(OutPutStream, "UTF-8"));

                bufferedWriterObject.write(bufferedWriterDataFN(PData));

                bufferedWriterObject.flush();

                bufferedWriterObject.close();

                OutPutStream.close();

                RC = httpURLConnectionObject.getResponseCode();

                if (RC == HttpsURLConnection.HTTP_OK) {

                    bufferedReaderObject = new BufferedReader(new InputStreamReader(httpURLConnectionObject.getInputStream()));

                    stringBuilder = new StringBuilder();

                    String RC2;

                    while ((RC2 = bufferedReaderObject.readLine()) != null) {

                        stringBuilder.append(RC2);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }

        private String bufferedWriterDataFN(HashMap<String, String> HashMapParams) throws UnsupportedEncodingException {

            StringBuilder stringBuilderObject;

            stringBuilderObject = new StringBuilder();

            for (Map.Entry<String, String> KEY : HashMapParams.entrySet()) {

                if (check)

                    check = false;
                else
                    stringBuilderObject.append("&");

                stringBuilderObject.append(URLEncoder.encode(KEY.getKey(), "UTF-8"));

                stringBuilderObject.append("=");

                stringBuilderObject.append(URLEncoder.encode(KEY.getValue(), "UTF-8"));
            }

            return stringBuilderObject.toString();
        }

    }


    public void dialog() {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Save Image To Gallery?");
        alertDialogBuilder.setTitle("No Internet!");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        saveImage_Function();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        image.setImageResource(R.drawable.no_image);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == SELECT_FILE)
//                onSelectFromGalleryResult(data);
//            else if (requestCode == REQUEST_CAMERA)
//                onCaptureImageResult(data);
//        }
//
//
//    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }


    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    //////////////
    //dialog
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery",
                "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
//                    image.setImageResource(R.drawable.no_image);
                    userChoosenTask = "Take Photo";
                    cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
//                    image.setImageResource(R.drawable.no_image);
                    userChoosenTask = "Choose from Gallery";
                    pictureFilePath="";
                    galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    //gallery
    private void galleryIntent() {
        Flag = 1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

    }


    Bitmap myBitmap_;
    int Flag = 0;

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            try {
                myBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());

                //dialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("BitmapEmpty", "true");
        }

        image.setImageBitmap(myBitmap);

    }


    //camera
    private void cameraIntent() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_CAMERA);
        sendTakePictureIntent();
    }


    //capture image process
    private void onCaptureImageResult(Intent data) {

        myBitmap = (Bitmap) data.getExtras().get("data");
        image.setImageBitmap(myBitmap);

        dialog();
    }


    //all permissions
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    ///////////////////////////////////////
    //save image to gallery from imageview


    public void refreshGallery(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }


    private String getCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static Bitmap viewToBitmap(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    private File getDisc() {
        String t = getCurrentDateAndTime();
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(file, "KPRA");
    }


    String saved_image_path;

    public void saveImage_Function() {

        FileOutputStream fos = null;
        File file = getDisc();
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(this, "Can't Save Image Allow Permission", Toast.LENGTH_LONG).show();
            //return;
//            Toast.makeText(Home.this, "file not created", Toast.LENGTH_LONG).show();
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmsshhmmss");
        String date = simpleDateFormat.format(new Date());
        String name = "FileName" + date + ".jpg";
        saved_image_path = file.getAbsolutePath() + "/" + name;
        File new_file = new File(saved_image_path);
        Log.e("[path", saved_image_path);
//        Toast.makeText(MainActivity.this, "new_file created", Toast.LENGTH_LONG).show();

        try {
            fos = new FileOutputStream(new_file);
            Bitmap bitmap = viewToBitmap(image, image.getWidth(), image.getHeight());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_LONG).show();
            fos.flush();
            fos.close();
            image.setImageResource(R.drawable.no_image);
        } catch (FileNotFoundException e) {
//            Toast.makeText(MainActivity.this, "FNF", Toast.LENGTH_LONG).show();

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshGallery(new_file);


    }
    ////////////////////////////


    ///////save captured image to gallery new method
    private static final String TAG = "CapturePicture";
    static final int REQUEST_PICTURE_CAPTURE = 1;
    private String pictureFilePath="";


    private void sendTakePictureIntent() {

//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);

            File pictureFile = null;
            try {
                pictureFile = getPictureFile();

            } catch (IOException ex) {
                Toast.makeText(this,
                        "Image can't be Saved, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.dzone.kpra_app",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }

    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "ZOFTINO_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && Flag == 1) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
        }

        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                image.setImageURI(Uri.fromFile(imgFile));
            }
        }
    }






    //////////////////


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menus, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logout) {

            logout();
        }
        return super.onOptionsItemSelected(item);
    }




    private void logout(){

        //Creating an alert dialog to confirm logout

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Logout!");
        alertDialogBuilder.setIcon(R.drawable.logo);
        alertDialogBuilder.setMessage("Are you sure you want to logout?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        //Getting out sharedpreferences
                        SharedPreferences preferences = getSharedPreferences("DataStore", Context.MODE_PRIVATE);
                        //Getting editor
                        SharedPreferences.Editor editor = preferences.edit();

                        //Putting blank value to number
                        editor.putString("name", "");


                        //Saving the sharedpreferences
                        editor.clear();
                        editor.commit();
                        finish();

                        //Starting login activity
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (new Check_internet_connection(getApplicationContext()).isNetworkAvailable()) {
            Log.e("NetworkState", "connected");

        }
        else {

            Toast.makeText(this, "No Internet! You Can Save Images To Gallery", Toast.LENGTH_SHORT).show();
        }
    }

    }
