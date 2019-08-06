package com.example.appchat.views.home.tabtimeline.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.appchat.R;
import com.example.appchat.callback.ILoadMore;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.objectclass.Status;
import com.example.appchat.views.home.ShareImageActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHodel> {

    private Context context;
    private List<Status> statuses;
    private SimpleDateFormat format;
    private SimpleDateFormat formatDay;

    private ILoadMore loadMore;
    private boolean isLoading;
    private int lastVisibleItem;

    public StatusAdapter(Context context, List<Status> listStatus) {
        this.context = context;
        this.statuses = listStatus;
        format = new SimpleDateFormat("HH:mm");
        formatDay = new SimpleDateFormat("dd/MM/yyyy");
    }

    int y;

    public void setRecyclerView(RecyclerView recyclerView) {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                y = dy;
                int last = linearLayoutManager.findLastVisibleItemPosition();
                if (last != -1) {
                    lastVisibleItem = last;
                }
                if (!isLoading && lastVisibleItem == (statuses.size() - 1)) {
                    if (loadMore != null) {
                        isLoading = true;
                        loadMore.onLoadMore();
                    }
                }
            }
        });
    }

    public int getY() {
        return y;
    }

    public void setLoadMore(ILoadMore loadMore) {
        this.loadMore = loadMore;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public boolean getLoaded() {
        return isLoading;
    }


    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_status, viewGroup, false);
        return new ViewHodel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Status status = statuses.get(i);
        viewHodel.txtName.setText(status.getUserid().getName());
        Date date = new Date(status.getCreatedate());
        viewHodel.txtBody.setText(status.getBody());

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        Calendar now2 = Calendar.getInstance();
        now2.setTime(date);
        int year = now2.get(Calendar.YEAR);
        int month = now2.get(Calendar.MONTH);
        int day = now2.get(Calendar.DAY_OF_MONTH);
        int hour = now2.get(Calendar.HOUR_OF_DAY);
        int minute = now2.get(Calendar.MINUTE);

        if (currentYear == year) {
            if (currentMonth == month) {
                if (currentDay == day) {
                    if (currentHour == hour) {
                        viewHodel.txtCreateDate.setText((currentMinute - minute + 1)
                                + " "
                                + context.getResources().getString(R.string.minute_ago));
                    } else {
                        viewHodel.txtCreateDate.setText((currentHour - hour)
                                + " "
                                + context.getResources().getString(R.string.hours_ago));
                    }
                } else {
                    if (currentDay - day >= 5) {
                        viewHodel.txtCreateDate.setText(formatDay.format(date));
                    } else {
                        if (currentDay - day == 1) {
                            viewHodel.txtCreateDate.setText(context.getResources().getString(R.string.yesterday));
                        } else {
                            viewHodel.txtCreateDate.setText((currentDay - day)
                                    + " "
                                    + context.getResources().getString(R.string.day_ago));
                        }
                    }
                }
            } else {
                if (currentMonth - month > 1) {
                    viewHodel.txtCreateDate.setText(formatDay.format(date));
                } else {
                    int dayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int distanceDay = dayOfMonth - day + currentDay;
                    viewHodel.txtCreateDate.setText(distanceDay
                            + " "
                            + context.getResources().getString(R.string.day_ago));
                }
            }
        } else {
            viewHodel.txtCreateDate.setText(formatDay.format(date));
        }

        Picasso.get().load(status.getUserid().getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imgAvatar);
        if (!status.getUrlImages().isEmpty()) {
            Glide.with(context)
                    .load(status.getUrlImages().get(0))
                    .placeholder(R.drawable.image40)
                    .error(R.drawable.image40)
                    .into(viewHodel.imgStatus);
            viewHodel.imgStatus.setVisibility(View.VISIBLE);

            viewHodel.imgStatus.setOnClickListener(t -> {
                if (viewHodel.imgStatus.getDrawable() != null) {
                    Intent intent = new Intent(context, ShareImageActivity.class);
                    Bitmap bitmap = ((BitmapDrawable) viewHodel.imgStatus.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("image", byteArray);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation((Activity) context, new Pair<View, String>(viewHodel.imgStatus
                                        , "imagetransition"));
                        context.startActivity(intent, options.toBundle());
                    } else {
                        context.startActivity(intent);
                    }
                }
            });
        } else {
            viewHodel.imgStatus.setVisibility(View.GONE);
        }
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.animation_button_like);

        viewHodel.imgLike.setOnClickListener(t -> {
            t.startAnimation(animation);
            if (iLike == 0) {
                viewHodel.imgLike.setImageResource(R.drawable.like);
                iLike = 1;
            } else {
                viewHodel.imgLike.setImageResource(R.drawable.unlike);
                iLike = 0;
            }
        });

        viewHodel.imgComment.setOnClickListener(t -> {

        });
    }

    int iLike = 0;

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    class ViewHodel extends RecyclerView.ViewHolder {
        private CircleImage imgAvatar;
        private TextView txtName, txtCreateDate, txtBody;
        private ImageView imgStatus, imgLike, imgComment;
//        private RecyclerView recyclerView;
//        private ImageStatusAdapter imageStatusAdapter;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name);
            txtCreateDate = itemView.findViewById(R.id.textview_createdate);
            txtBody = itemView.findViewById(R.id.textview_body);
            imgStatus = itemView.findViewById(R.id.imageview_status);
            imgLike = itemView.findViewById(R.id.img_like);
            imgComment = itemView.findViewById(R.id.img_comment);
//            recyclerView = itemView.findViewById(R.id.recyclerview_image_status);
        }

//        public void initRecyclerViewImageStatus(List<String> list) {
//
//            DisplayMetrics displaymetrics = new DisplayMetrics();
//            ((HomeActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//            int screenHeight = displaymetrics.heightPixels;
//            int screenWidth = displaymetrics.widthPixels;
//            float density = context.getResources().getDisplayMetrics().density;
//            float pxTodpHeight = screenHeight / density;
//            float pxTodpWidth = screenWidth / density;
//            float widthImage = pxTodpWidth / 3;
//            GridLayoutManager layoutManager;
//            int size = list.size();
//            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams();
//            switch (size) {
//                case 1:
//                    layoutManager = new GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false);
//                    break;
//                case 2:
//                    layoutManager = new GridLayoutManager(context, 2, LinearLayoutManager.HORIZONTAL, false);
//                    break;
//                case 3:
//                    break;
//                case 4:
//                    break;
//                case 5:
//                    break;
//                case 6:
//                    break;
//                case 7:
//                    break;
//                case 8:
//                    break;
//                case 9:
//                    break;
//            }
//
//
//            recyclerView.setLayoutManager(layoutManager);
//            imageStatusAdapter = new ImageStatusAdapter(context, list);
//            recyclerView.setAdapter(imageStatusAdapter);
//        }


    }
}
