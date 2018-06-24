package com.example.zk3;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView choicePictureType;
    private RecyclerView friendCircleList;
    List<Bitmap> dataSoucrce = new ArrayList<>();
    Intent intent;
    RecyclerViewAdapter recyclerViewAdapter;
    Uri takePictureURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        recyclerViewAdapter = new RecyclerViewAdapter(dataSoucrce, MainActivity.this);
        friendCircleList.setAdapter(recyclerViewAdapter);
    }
    private void initView() {
        choicePictureType = (ImageView) findViewById(R.id.choicePictureType);
        choicePictureType.setOnClickListener(this);
        friendCircleList = (RecyclerView) findViewById(R.id.friendCircleList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        friendCircleList.setLayoutManager(linearLayoutManager);
    }
    class RecyclerViewAdapter extends Adapter<RecyclerViewAdapter.MyViewHodler> {
        private List<Bitmap> dataSoucrce ;
        private Context context;

        public RecyclerViewAdapter(List<Bitmap> dataSoucrce, Context context) {
            this.dataSoucrce = dataSoucrce;
            this.context = context;
        }
        @NonNull
        @Override
        public RecyclerViewAdapter.MyViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.friendcircle_item, null);
            MyViewHodler myViewHodler = new MyViewHodler(view);
            return myViewHodler;
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.MyViewHodler holder, int position) {
            holder.recycler_friend_list.setImageBitmap(dataSoucrce.get(position));
        }
        @Override
        public int getItemCount() {
            return dataSoucrce.size();
        }
        public class MyViewHodler extends RecyclerView.ViewHolder {
            ImageView recycler_friend_list;
            public MyViewHodler(View itemView) {
                super(itemView);
                recycler_friend_list = itemView.findViewById(R.id.recycler_friend_list); }}}
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choicePictureType:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final AlertDialog alertDialog = builder.create();
                View view_choicePicture = LayoutInflater.from(MainActivity.this).inflate(R.layout.choice_picture_type, null);
                alertDialog.setView(view_choicePicture);
                alertDialog.show();
               TextView take_picture =  view_choicePicture.findViewById(R.id.take_picture);
               TextView xiangce =  view_choicePicture.findViewById(R.id.xiangce);
                xiangce.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // checkSelfPermission(@NonNull Context context, @NonNull String permission)
                        //检查动态权限，这里检查的是用户是否授予了读写外部存储设备的权限
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                            alertDialog.dismiss();
                        }else {

                            if (Build.VERSION.SDK_INT < 19) {
                                intent = new Intent(Intent.ACTION_GET_CONTENT);
                                startActivity(intent);
                                intent.setType("image/*");
                            } else {
                                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            }
                            startActivityForResult(intent, 100);
                            alertDialog.dismiss();
                        }
                    }
                });
                take_picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        try {
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (Build.VERSION.SDK_INT >= 24) {
                            takePictureURL = FileProvider.getUriForFile(MainActivity.this, "com.example.zk3.fileProvider", outputImage);
                        }else {
                            takePictureURL = Uri.fromFile(outputImage);
                        }
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, takePictureURL);
                        startActivityForResult(intent,200);
                        alertDialog.dismiss();
                    }
                });
                break;}}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//data代表intent请求返回的数据
        super.onActivityResult(requestCode, resultCode, data);
        String imagPath = null;
        if (requestCode == 100) {
            Uri uri = data.getData();
            String path = uri.getPath();//这里后去的path是内容提供者的路径
            String[] str = {MediaStore.Images.Media.DATA};
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri,null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    imagPath =  cursor.getString(cursor.getColumnIndex(str[0])); }}
            Bitmap bitmap = BitmapFactory.decodeFile(imagPath);
            dataSoucrce.add(bitmap);
            recyclerViewAdapter.notifyDataSetChanged();
        }
        if (requestCode == 200) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(takePictureURL));
                dataSoucrce.add(bitmap);
                recyclerViewAdapter.notifyDataSetChanged();
            } catch (FileNotFoundException e) {
                e.printStackTrace(); }}}
    //动态权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (Build.VERSION.SDK_INT < 19) {
                  intent = new Intent(Intent.ACTION_GET_CONTENT);
                    startActivity(intent);
                    intent.setType("image/*");
                } else {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(intent, 100); }}}
