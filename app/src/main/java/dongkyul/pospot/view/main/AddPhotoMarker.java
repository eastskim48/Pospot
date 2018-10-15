package dongkyul.pospot.view.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import dongkyul.pospot.R;
import dongkyul.pospot.view.common.BaseActivity;


public class AddPhotoMarker extends BaseActivity {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;

    private Uri mImageCaptureUri;
    private ImageView viewImage1;
    private int id_view;
    private String absolutePath;

    private DB_Manager db_manager;

    public Button btnHome;
    public Button btnViewFilter;
    public Button btnAddPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photomarker);
        init();

        db_manager = new DB_Manager(this);

        // 클릭 마커 위치
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        double pointLat = extras.getDouble("pointLat");
        double pointLon = extras.getDouble("pointLon");
        Toast.makeText(
                getApplicationContext(),
                "앨범이 저장될 위치 - \n위도: " + pointLat + "\n경도: " + pointLon,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void init() {
        super.init();
        viewImage1 = (ImageView) this.findViewById(R.id.img1);
        btnHome = (Button)findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);
        btnViewFilter = (Button)findViewById(R.id.btnViewFilter);
        btnViewFilter.setOnClickListener(this);
        btnAddPhoto = (Button)findViewById(R.id.btnAddPhoto);
        btnAddPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btnHome:
                Intent intent = new Intent(AddPhotoMarker.this, MainMapActivity.class);
                startActivity(intent);
                Toast.makeText(
                        getApplicationContext(),
                        "홈버튼클릭!",
                        Toast.LENGTH_LONG).show();
                break;
            case R.id.btnViewFilter:
                // to-be
                // 필터 사진 목록보기로 이동
                break;
            case R.id.btnAddPhoto:
                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakeAlbumAction();
                    }
                };
                DialogInterface.OnClickListener cancleListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancleListener)
                        .show();
                break;
        }
    }

    /*
     * 앨범에서 이미지 가져오기
     * */
    public void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK)
            return;
        switch(requestCode) {
            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
                Log.d("PICK_FROM_ALBUM??", mImageCaptureUri.getPath().toString());
            }
            case PICK_FROM_CAMERA:
            {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_iMAGE);
                break;
            }
            case CROP_FROM_iMAGE:
            {
                if(resultCode!=RESULT_OK) {
                    return;
                }
                final Bundle extras = data.getExtras();
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pospot/"+System.currentTimeMillis()+".jpg";
                if(extras!=null) {
                    Bitmap photo = extras.getParcelable("data");
                    viewImage1.setImageBitmap(photo);
                    storeCropImage(photo,filePath);
                    absolutePath = filePath;
                    break;
                }
                File f = new File(mImageCaptureUri.getPath());
                if(f.exists()) {
                    f.delete();
                }
            }
        }
    }

    /*
     * Bitmap 저장
     * */
    private void storeCropImage(Bitmap bitmap, String filePath) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pospot";
        File directory_Pospot = new File(dirPath);
        if(!directory_Pospot.exists()) {
            directory_Pospot.mkdir();
        }
        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}