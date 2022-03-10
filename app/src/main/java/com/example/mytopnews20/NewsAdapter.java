package com.example.mytopnews20;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** 这里是RecyclerVieww适配器的标准写法，也即必须重写以下三个方法
 *  这里我们自定义一个适配器继承自RecyclerView.Adapter, 并且我们需要将泛型指定为NewsAdapter.ViewHolder
 *  其中ViewHolder是NewsAdapter里面的一个内部类
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsDirection> newsDirections;

    // 这里我们定义一个内部类ViewHolder，其作用是在RecyclerView滚动时设置值
    static class  ViewHolder extends RecyclerView.ViewHolder {
        View newsView;  // 新闻界面
        ImageView newsImage;
        TextView newsIntroduction;
        TextView mediaName;
        TextView punishTime;
        public ViewHolder(View view) {
            super(view);
            newsView = view;
            newsImage = view.findViewById(R.id.news_image);
            newsIntroduction = view.findViewById(R.id.news_introduction);
            mediaName = view.findViewById(R.id.media_name);
            punishTime = view.findViewById(R.id.punish_time);
        }
    }

    // 构造函数，用于传入数据源
    public NewsAdapter(List<NewsDirection> newsDirections) {
        this.newsDirections = newsDirections;
    }

    // 以下三个函数一般都需要重写
    // 其中onCreateViewHolder方法用于创建ViewHolder实例， 在这个方法当中
    // 我们需要把RecyclerView的子项布局加载进来，并返回ViewHolder的实例
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.news_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        // 添加点击事件
        holder.newsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                NewsDirection newsDirection = newsDirections.get(position);
                Context context = view.getContext();
                Intent intent = new Intent(context, NewsWebView.class);
                intent.putExtra("urlId", newsDirection.getUrlId());
                context.startActivity(intent);
            }
        });
        return holder;
    }

    // onBindViewHolder方法用于对RecyclerView子项的数据进行赋值， 会在每个子项
    // 被滚动到屏幕内的时候执行，我们通过position参数获得当前的NewsDirection实例
    // 然后将数据设置到ViewHolder的newsImage和newsIntroduction中即可
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NewsDirection newsDirection = newsDirections.get(position);
//        holder.newsImage.setImageResource(newsDirection.getImageId());
        // 这里需要将图片的byte数组转化成ImageView可调用的Bitmap对象
        byte [] image_id = newsDirection.getImageId();
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_id,
                0, image_id.length);
        holder.newsImage.setImageBitmap(bitmap);
        holder.newsIntroduction.setText(newsDirection.getIntroduction());
        holder.mediaName.setText(newsDirection.getMediaName());
        holder.punishTime.setText(newsDirection.getPunish_time());
    }

    //  getItemCount方法用于告诉RecyclerView一共有几个子项，这里我们直接返回数据源的长度
    @Override
    public int getItemCount() {
        return newsDirections.size();
    }
}
