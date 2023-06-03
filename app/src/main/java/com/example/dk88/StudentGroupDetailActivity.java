package com.example.dk88;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentGroupDetailActivity extends AppCompatActivity {

    TextView tvLostCourse, tvDetail, tvJoined, tvWaiting, tvPhoneNumber;
    String lostCourse, detail, joined, waiting, phoneNumber;

    Button btnVote;
    ImageView ivBack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_group_detail_layout);

        tvLostCourse =(TextView) findViewById(R.id.giveClass);
        tvDetail = (TextView) findViewById(R.id.detail);
        tvJoined = (TextView) findViewById(R.id.joinList);
        tvWaiting = (TextView) findViewById(R.id.waitingList);
        tvPhoneNumber = (TextView) findViewById(R.id.phoneNumber);
        btnVote = (Button) findViewById(R.id.vote_button);
        ivBack = (ImageView) findViewById(R.id.back);

        String token="";
        String studentID="";
        HashMap<String, String> needClass = new HashMap<>();
        GroupInfo groupInfo = new GroupInfo();
        lostCourse = "Summary: To get class you want, you will have to give class ";
        detail="Detail: \n";
        joined = "Joined: ";
        waiting = "Waiting: ";
        phoneNumber = "Phone number: \n";

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            needClass = (HashMap<String, String>) bundle.getSerializable("needClass");
            studentID = bundle.getString("studentID");
            token = bundle.getString("token");
            groupInfo = (GroupInfo) bundle.getSerializable("groupInfo");
            if (needClass != null && token != null && groupInfo != null) {
                String[] members = groupInfo.getGroupID().split("-");
                HashMap<String, String> lost = new HashMap<>();
                for (int i=0;i<members.length;i++){
                    if (i==0){
                        lost.put(members[0],needClass.get(members[members.length-1]));
                    }else{
                        lost.put(members[i],needClass.get(members[i-1]));
                    }

                    Map<String, Object> headers = new HashMap<>();
                    headers.put("token", token);
                    Call<ResponseObject> call = ApiUserRequester.getJsonPlaceHolderApi().getStudentInfo(headers, members[i]);
                    int finalI = i;
                    call.enqueue(new Callback<ResponseObject>() {
                        @Override
                        public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                            if (!response.isSuccessful()) {
                                Toast.makeText(StudentGroupDetailActivity.this, "Error", Toast.LENGTH_LONG).show();
                                return;
                            }
                            ResponseObject tmp = response.body();

                            if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                                Toast.makeText(StudentGroupDetailActivity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }
                            Map<String, Object> data = (Map<String, Object>) tmp.getData();
                            phoneNumber+=members[finalI] +": " + data.get("phoneNumber")+"\n";
                            tvPhoneNumber.setText(phoneNumber);
                        }
                        @Override
                        public void onFailure(Call<ResponseObject> call, Throwable t) {
                        }
                    });


                }
                for (String member: members){
                    detail+= member + " have class " + lost.get(member)+"\n";
                }

                tvLostCourse.setText(lostCourse+groupInfo.getLophp());
                tvDetail.setText(detail);



                Map<String,Object> headers=new HashMap<>();
                headers.put("token",token);

                Call<ResponseObject> call = ApiUserRequester.getJsonPlaceHolderApi().getGroupInfo(headers, groupInfo.getGroupID());
                String finalStudentID = studentID;
                call.enqueue(new Callback<ResponseObject>() {
                    @Override
                    public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                        if (!response.isSuccessful())
                        {
                            Toast.makeText(StudentGroupDetailActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ResponseObject tmp = response.body();
                        if (tmp.getRespCode()!=ResponseObject.RESPONSE_OK)
                        {
                            Toast.makeText(StudentGroupDetailActivity.this, tmp.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Map<String, Object> data = (Map<String, Object>) tmp.getData();
                        ArrayList<String> voteYes = (ArrayList<String>) data.get("voteYes");
                        joined+= TextUtils.join(",", voteYes);
                        tvJoined.setText(joined);

                        ArrayList<String> waitList = new ArrayList<>();
                        for (String member: members){
                            if (!voteYes.contains(member)){
                                waitList.add(member);
                            }
                        }
                        waiting += TextUtils.join(", ", waitList);
                        tvWaiting.setText(waiting);

                        if (waiting.contains(finalStudentID)){
                            btnVote.setText("JOIN");
                        }else{
                            btnVote.setText("LEAVE");
                        }
                    }


                    @Override
                    public void onFailure(Call<ResponseObject> call, Throwable t) {

                    }
                });
            }

        }

        String finalToken = token;
        String finalStudentID1 = studentID;
        GroupInfo finalGroupInfo = groupInfo;
        btnVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> headers = new HashMap<>();
                Map<String, Object> body = new HashMap<>();

                headers.put("token", finalToken);
                body.put("studentID", finalStudentID1);
                body.put("groupID", finalGroupInfo.getGroupID());

                Call<ResponseObject> call = ApiUserRequester.getJsonPlaceHolderApi().voteGroup(headers,body);
                call.enqueue(new Callback<ResponseObject>() {
                    @Override
                    public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(StudentGroupDetailActivity.this, "Error", Toast.LENGTH_LONG).show();
                            return;
                        }
                        ResponseObject tmp = response.body();

                        if (tmp.getRespCode() != ResponseObject.RESPONSE_OK) {
                            Toast.makeText(StudentGroupDetailActivity.this, tmp.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (btnVote.getText().equals("JOIN")){
                            btnVote.setText("LEAVE");
                        }else{
                            btnVote.setText("JOIN");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseObject> call, Throwable t) {

                    }
                });

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
