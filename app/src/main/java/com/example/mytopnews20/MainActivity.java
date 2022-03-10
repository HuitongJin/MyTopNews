package com.example.mytopnews20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<List<NewsDirection>> listArray = new ArrayList<>(6); // 定义一个二维数组，存放六个界面的新闻内容
    private List<View> viewList = new ArrayList<>(); // 用于存放六个页面的view
    private TabLayout tabLayout; // 使用tabLayout设置导航栏， 实现带有标志的导航效果
    private ViewPager viewPager; // 使用ViewPager存放六个view， 实现左右滑动翻页， 并和tabLayout联动
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPagerAdapter myAdapter;
    private NewsAdapter[] adapterArray = new NewsAdapter[6];
    private String urlStart = "https://i.news.qq.com/trpc.qqnews_web.kv_srv.kv_srv_http_proxy/list?sub_srv_id=24hours&srv_id=pc&offset=";
    private String urlEnd = "&limit=7&strategy=1&ext={\"pool\":[\"top\"],\"is_filter\":7,\"check_type\":true}";
    private int offset = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
        // 为每个item添加点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_personal:
                        Toast.makeText(MainActivity.this, "You clicked Personal Center",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_change:
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("Reminder:");
                        dialog.setMessage("Are you sure you want to switch accounts ?");
                        dialog.setCancelable(false);
                        //dialog.setPositiveButtonIcon(R.drawable.ok);
                        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish(); // 记得finish，否则没有实现退出当前账号
                            }
                        });
                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        dialog.show();
                        break;
                    case R.id.nav_collection:
                        Toast.makeText(MainActivity.this, "You clicked My Collection",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_setting:
                        Toast.makeText(MainActivity.this, "You clicked Setting",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_connect:
                        // 检查用户是否授权
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                                permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                    {Manifest.permission.CALL_PHONE}, 1);
                        }else{
                            call();  // 如果已授权，则调用我们自定义的call函数
                        }
                        break;
                        default:
                            break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        initMenuTabs(); // 初始化导航栏tabLayout内容
        initNewsInfo(); // 初始化新闻信息
        initViewPager(); // 初始化viewPager页面信息
        BindTabAndPager(); // 绑定tabLayout和viewPager，实现联动

        swipeRefreshLayout = findViewById(R.id.wipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offset = (offset+8)%100;
                getNewsFromTencent(offset);
                swipeRefreshLayout.setRefreshing(false); // 将进度条隐藏
                Toast.makeText(MainActivity.this, "Finish updating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 重写menu的两个方法，实现DrawerLayout的导航按钮事件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.update:
                offset = (offset+8)%100;
                getNewsFromTencent(offset);
                Toast.makeText(MainActivity.this, "Finish updating", Toast.LENGTH_SHORT).show();
                break;
                default:
                    break;
        }
        return true;
    }

    private void initMenuTabs() {
        tabLayout = this.findViewById(R.id.menuTab);
        // 使用addTab函数添加tab， 其中，使用newTab创建一个新的tab，使用setText设置文字，使用setIcon设置图标
        tabLayout.addTab(tabLayout.newTab().setText("Entertainment").setIcon(R.drawable.entertainment));
        tabLayout.addTab(tabLayout.newTab().setText("Learning").setIcon(R.drawable.learning));
        tabLayout.addTab(tabLayout.newTab().setText("SchoolNews").setIcon(R.drawable.school));
        tabLayout.addTab(tabLayout.newTab().setText("Sports").setIcon(R.drawable.sports));
        tabLayout.addTab(tabLayout.newTab().setText("Shopping").setIcon(R.drawable.shoping));
        tabLayout.addTab(tabLayout.newTab().setText("Others").setIcon(R.drawable.others));
    }

    // 设置新闻内容
    private void initNewsInfo() {
        List<NewsDirection> list = getNewsFromTencent(offset);
        for (int i = 0; i < 6; i++) {
            Collections.shuffle(list);
            listArray.add(list);
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    List<NewsDirection> list1 = (List) msg.obj;
                    int index = viewPager.getCurrentItem(); // 获取当前ViewPager的item
                    for (int i=0; i<list1.size(); i++){
                        listArray.get(index).set(i, list1.get(i));
                    }
                    adapterArray[viewPager.getCurrentItem()].notifyDataSetChanged(); // 更新对应的适配器
                    break;
                }
            }
    };

    // 从网页获取json数据并进行解析是一个耗时操作，需要开启一个线程
    private  List<NewsDirection> getNewsFromTencent(int Newoffset) {
        final List<NewsDirection> list = new ArrayList<>();
        final int this_offset = Newoffset;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = urlStart + this_offset + urlEnd; // 获取网址
                try {
                    Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; " +
                            "Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299")
                            .timeout(30000).ignoreContentType(true).get();
                    Element data = document.body();
                    String data_js = data.text(); //获取json数据

                    // 使用JSONObject进行解析
                    try {
                        JSONObject jsonObject = new JSONObject(data_js);
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String title = jsonObject1.getString("title");
                            String news_url = jsonObject1.getString("url");

                            byte[] bytes;
                            String image_url = jsonObject1.getString("thumb_nail");
                            String media_name = jsonObject1.getString("media_name");
                            String punish_time = jsonObject1.getString("publish_time");
                            URL url1 = new URL(image_url);
                            HttpURLConnection connection = (HttpURLConnection)url1.openConnection();
                            connection.setConnectTimeout(5000);
                            connection.setRequestMethod("GET");
                            int code = connection.getResponseCode();
                            if(code == 200) {
                                InputStream inputStream = connection.getInputStream();
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int len;
                                while((len = inputStream.read(buffer)) != -1) {
                                    byteArrayOutputStream.write(buffer, 0, len);
                                }
                                bytes = byteArrayOutputStream.toByteArray();
                                NewsDirection newsDirection = new NewsDirection(title, bytes, news_url,media_name, punish_time);
                                list.add(newsDirection);

                            }
                        }

                    }catch (JSONException je) {
                        je.printStackTrace();
                    }

                }catch (IOException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.obj = list;
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();
        return list;
    }



    private void initViewPager() {

        /** LayoutInflater是一个用于解析xml的类，它的作用类似于findViewById，
         *不同的是，LayoutInflater是用来找res/layout下的xml布局文件，并且实例化，而findViewById是
         * 查找xml布局文件下的具体widget控件，如Button，TextView等。
         * 1. 对于一个没有载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入
         * 2. 对于一个已经载入的界面，就可以使用Activity.findViewById()方法来获得其中的界面元素
         */

        LayoutInflater inflater = getLayoutInflater(); // 实例化inflater
        // 这里注意：对于不同的recyclerView需要使用不同的layoutManager。 因此定义一个LinearLayoutManager数组
        LinearLayoutManager layoutManagerArray[] = new LinearLayoutManager[6];
        RecyclerView recyclerViewArray[] = new RecyclerView[6];
        View viewArray[] = new View[6];
        // News适配器用于向recyclerView传输信息
//         NewsAdapter[] adapterArray = new NewsAdapter[6];

        // 这里实现每个recyclerView的初始化
        for (int i = 0; i < 6; i++) {
            layoutManagerArray[i] = new LinearLayoutManager(this);
            viewArray[i] = inflater.inflate(R.layout.recyclerview, null);
            recyclerViewArray[i] = viewArray[i].findViewById(R.id.recyclerView_1);
            recyclerViewArray[i].setLayoutManager(layoutManagerArray[i]);
            adapterArray[i] = new NewsAdapter(listArray.get(i));
            recyclerViewArray[i].setAdapter(adapterArray[i]);
            viewList.add(recyclerViewArray[i]);
//            viewList.add(viewArray[i].findViewById(R.id.swipe_refresh));
        }
        // 将recyclerView嵌套到ViewPager里面
        viewPager = this.findViewById(R.id.my_viewPager);
        myAdapter = new ViewPagerAdapter(viewList);
        viewPager.setAdapter(myAdapter);
    }

    private void BindTabAndPager() {
        // 这个地方必须重新加载View，否则系统会找不到tabLayout和viewPager
        tabLayout = this.findViewById(R.id.menuTab);
        viewPager = this.findViewById(R.id.my_viewPager);
        // Viewpager的监听（这个接听是为TabLayout专门设计的）
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //TabLayout的监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            // 被选中的时候
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
            }
            // 没有被选中的时候
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            // 重现被选中的时候
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void call() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:950800"));
            startActivity(intent);
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length !=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    call();
                }else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
                default:
        }
    }


}
