package com.example.wherearyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.wherearyou.MainActivity.fragHome;

public class ListViewAdapter extends BaseAdapter {
    Bitmap bitmap;
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

    public ListViewAdapter(){ }

    @Override
    public int getCount(){
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_friends_info, parent, false);
        }

        final CircleImageView iconImageView = (CircleImageView) convertView.findViewById(R.id.friend_photo);
        final TextView friendNameView = (TextView) convertView.findViewById(R.id.friend_name);
        final Button friendSearchBtn = (Button) convertView.findViewById(R.id.search_btn);
        final Button friendSearchingBtn = (Button) convertView.findViewById(R.id.searching_btn);
        final Button friendLocationApply = (Button) convertView.findViewById(R.id.sub_apply_btn);
        final Button friendLocationReject = (Button) convertView.findViewById(R.id.sub_reject_btn);
        final Button locationSharing = (Button) convertView.findViewById(R.id.sharing_btn);
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

        final ListViewItem listViewItem = listViewItemList.get(position);
        Log.d(TAG, "순서2");

        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {

                    URL url = new URL(listViewItem.getIcon());


                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();

                    bitmap = BitmapFactory.decodeStream(is);

                    Log.d(TAG, "에러이름" + listViewItem.getFriendName());
                    Log.d(TAG, "에러사진" + listViewItem.getIcon());
                    iconImageView.setImageBitmap(bitmap);
                    friendNameView.setText(listViewItem.getFriendName());


                } catch (IOException ex) {

                }
            }
        };
        mThread.start();
        try {
            mThread.join();
            Log.d(TAG, "에러이름" + listViewItem.getFriendName());
            Log.d(TAG, "에러사진" + listViewItem.getIcon());
            iconImageView.setImageBitmap(bitmap);
            friendNameView.setText(listViewItem.getFriendName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final DatabaseReference locationPermission = mReference.child("User").child(listViewItem.getFriendName()).child("위치정보요청").child("아이디");
        final DatabaseReference locationPermissionMe = mReference.child("User").child(ToDB.EmailToId).child("위치정보요청").child("아이디");
        final DatabaseReference sharing = mReference.child("User").child(listViewItem.getFriendName()).child("위치정보허용").child("상태");
        locationPermissionMe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String locationPermissionId = dataSnapshot.getValue(String.class);

                friendSearchBtn.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        friendSearchBtn.setVisibility(GONE);
                        friendSearchingBtn.setVisibility(VISIBLE);
                        friendLocationApply.setVisibility(GONE);
                        friendLocationReject.setVisibility(GONE);
                        locationSharing.setVisibility(GONE);

                        locationPermission.setValue(ToDB.EmailToId);
                    }
                });

                if(listViewItem.getFriendName().equals(locationPermissionId)){
                    friendSearchBtn.setVisibility(GONE);
                    friendSearchingBtn.setVisibility(GONE);
                    friendLocationApply.setVisibility(VISIBLE);
                    friendLocationReject.setVisibility(VISIBLE);
                    locationSharing.setVisibility(GONE);
                    //연결끊기 버튼 만들고 액션 생성
                    friendLocationApply.setOnClickListener(new Button.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            // 수락시 상대방에게 동의했다고 알림
                            sharing.setValue(true);
                            friendSearchBtn.setVisibility(GONE);
                            friendSearchingBtn.setVisibility(GONE);
                            friendLocationApply.setVisibility(GONE);
                            friendLocationReject.setVisibility(GONE);
                            locationSharing.setVisibility(VISIBLE);

                            FragHome.locationPermissionBoolean = true;
                        }
                    });

                    friendLocationReject.setOnClickListener(new Button.OnClickListener(){
                        @Override
                        public void onClick(View v){

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return convertView;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public Object getItem(int position){
        return listViewItemList.get(position);
    }

    public void addItem(String name, String photoUrl){
        ListViewItem item = new ListViewItem();

        Log.d(TAG, "순서1");
        Log.d(TAG, "순서밑에" + photoUrl);
        Log.d(TAG, "순서밑에" + name);
        item.setIcon(photoUrl);
        item.setFriendName(name);
        listViewItemList.add(item);
    }
}