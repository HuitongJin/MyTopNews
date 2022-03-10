package com.example.mytopnews20;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;  // 创建数据库实例
    private EditText accountEdit; // 创建EditText实例，用于获取用户账号
    private EditText passwordEdit; // 创建EditText实例，用于获取用户密码
    private Button login;  // 登录按钮
    private Button create; // 注册按钮
    private String this_name;
    private String this_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 隐藏系统自带的标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        accountEdit = findViewById(R.id.user_name);
        passwordEdit = findViewById(R.id.pass_word);
        login = findViewById(R.id.login);
        create = findViewById(R.id.create_account);

        dbHelper = new MyDatabaseHelper(this, "Account.db", null, 1);

        // 为login注册点击事件，如果用户名和账号匹配则登录成功，跳转活动
        // 否则使用Toast发出错误提示，另外如果账户或者密码没有填写也发出提示
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断账号和密码是否填写
                if("".equals(accountEdit.getText().toString()) || "".equals(passwordEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Please fill in the account and password",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    this_name = accountEdit.getText().toString();
                    this_password = passwordEdit.getText().toString();
                    // 判断账号密码是否正确
                    Cursor cursor = db.query("Account", null, null, null,
                            null, null, null, null);
                    boolean flag = false;
                    // 遍历数据库，判断账户密码是否匹配
                    if(cursor.moveToFirst()) {
                        do{
                            String name = cursor.getString(cursor.getColumnIndex("userName"));
                            String password = cursor.getString(cursor.getColumnIndex("passWord"));
                            if(this_name.equals(name) && this_password.equals(password)) {
                                flag = true;
                                Toast.makeText(LoginActivity.this, "Login succeeded",
                                        Toast.LENGTH_SHORT).show();
                                cursor.close();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            }
                        }while (cursor.moveToNext());
                    }
                    if(!flag)
                        Toast.makeText(LoginActivity.this, "Account or Password is invalid",
                                Toast.LENGTH_SHORT).show();
                    cursor.close();
                }
            }
        });

        // 为create添加点击事件，逻辑和login类似
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("".equals(accountEdit.getText().toString()) || "".equals(passwordEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "Please fill in the account and password",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    this_name = accountEdit.getText().toString();
                    this_password = passwordEdit.getText().toString();
                    // 判断账号是否存在
                    Cursor cursor = db.query("Account", null, null, null,
                            null, null, null, null);
                    boolean flag = false;
                    if(cursor.moveToFirst()) {
                        do{
                            String name = cursor.getString(cursor.getColumnIndex("userName"));
                            if(this_name.equals(name)) {
                                flag = true;
                                Toast.makeText(LoginActivity.this, "Account already exists",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }while (cursor.moveToNext());
                    }
                    cursor.close();
                    if(!flag) {
                        ContentValues values = new ContentValues();
                        values.put("userName", this_name);
                        values.put("passWord", this_password);
                        db.insert("Account", null, values);
                        Toast.makeText(LoginActivity.this, "Create succeeded",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }
}
