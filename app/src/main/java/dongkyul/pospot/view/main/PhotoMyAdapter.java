package dongkyul.pospot.view.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import dongkyul.pospot.R;
//
//import static android.app.Activity.RESULT_OK;
//import static android.support.v4.app.ActivityCompat.startActivityForResult;

// Activity의 context this 를 어댑터로 넘겨줘..?

public class PhotoMyAdapter extends RecyclerView.Adapter<PlaceViewHolder> {

    private Context mContext;
    private int[] mPlaceList;


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


    public PhotoMyAdapter(Context mContext, int[] mPlaceList) {
        this.mContext = mContext;
        this.mPlaceList = mPlaceList;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if(viewType == R.layout.activity_photo_add_button){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_photo_add_button, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_photo_recyclerview_custom_layout,  parent, false);
        }

        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, int position) {

        if(position == mPlaceList.length) {
            holder.addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(mContext,"버튼클릭!",Toast.LENGTH_LONG);
                    Log.e("click button!?!?","ok");
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

                }
            });
        }
        else {
            holder.mPlace.setImageResource(mPlaceList[position]);
            holder.mPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(mContext, PhotoDetailActivity.class);
                    mIntent.putExtra("Image", mPlaceList[holder.getAdapterPosition()]);
                    mContext.startActivity(mIntent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPlaceList.length+1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mPlaceList.length) ? R.layout.activity_photo_add_button : R.layout.activity_photo_recyclerview_custom_layout;
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

class PlaceViewHolder extends RecyclerView.ViewHolder {

    ImageView mPlace;
    Button addBtn;

    public PlaceViewHolder(View itemView) {
        super(itemView);

        mPlace = itemView.findViewById(R.id.ivPlace);
        addBtn = itemView.findViewById(R.id.btnAddPhoto);
    }
}