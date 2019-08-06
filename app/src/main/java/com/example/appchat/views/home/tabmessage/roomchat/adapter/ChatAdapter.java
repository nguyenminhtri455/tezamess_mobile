package com.example.appchat.views.home.tabmessage.roomchat.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.appchat.R;
import com.example.appchat.callback.ILoadMore;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.database.TableContact;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.views.home.ShareImageActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final int ADMIN = 1;
    private final int CONTACT = 2;
    private Context context;
    private List<ChatMessage> chatMessages;
    private Member admin;

    private ILoadMore loadMore;
    private boolean isLoading;
    private int fristVisibleItem;

    public ChatAdapter(RecyclerView recyclerView, Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        admin = Member.getInstance(context);

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                fristVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                if (!isLoading && fristVisibleItem == 0 && chatMessages.size() > 10) {
                    if (loadMore != null) {
                        loadMore.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewtype) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        switch (viewtype) {
            case ADMIN:
                View viewAdmin = layoutInflater.inflate(R.layout.custom_content_message_admin, viewGroup, false);
                return new ViewHodelAdmin(viewAdmin);
            case CONTACT:
                View viewContact = layoutInflater.inflate(R.layout.custom_content_message_contact, viewGroup, false);
                return new ViewHodelContact(viewContact);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
        SimpleDateFormat formatSection = new SimpleDateFormat("dd/MM");
        ChatMessage chatMessage = chatMessages.get(i);
        String sectionCurrent = formatSection.format(new Date(chatMessage.getCreatedate()));
        switch (getItemViewType(i)) {
            case ADMIN:
                ViewHodelAdmin viewHodelAdmin = (ViewHodelAdmin) viewHolder;
                if (i - 1 == -1) {
                    viewHodelAdmin.txtSection.setText(sectionCurrent);
                    viewHodelAdmin.linearLayout.setVisibility(View.VISIBLE);

                } else {
                    String sectionBefore = formatSection.format(new Date(chatMessages.get(i - 1).getCreatedate()));
                    if (sectionCurrent.equals(sectionBefore)) {
                        viewHodelAdmin.linearLayout.setVisibility(View.GONE);
                    } else {
                        viewHodelAdmin.txtSection.setText(sectionCurrent);
                        viewHodelAdmin.linearLayout.setVisibility(View.VISIBLE);
                    }
                }
                switch (chatMessage.getTypeMessage()) {
                    case Chat:
                        viewHodelAdmin.txtContent.setText(chatMessages.get(i).getBody());
                        viewHodelAdmin.txtContent.setVisibility(View.VISIBLE);
                        viewHodelAdmin.imgContent.setVisibility(View.GONE);
                        break;
                    case Image:
                        viewHodelAdmin.txtContent.setVisibility(View.GONE);
                        viewHodelAdmin.imgContent.setVisibility(View.VISIBLE);
                        Glide.with(context).load(chatMessage.getBody())
                                .placeholder(R.drawable.placeholderimage)
                                .error(R.drawable.placeholderimage)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .fitCenter()
                                .override(500, 500)
                                .into(viewHodelAdmin.imgContent);

                        viewHodelAdmin.imgContent.setOnClickListener(t -> {
                            if (viewHodelAdmin.imgContent.getDrawable() != null) {
                                Intent intent = new Intent(context, ShareImageActivity.class);
                                Bitmap bitmap = ((BitmapDrawable) viewHodelAdmin.imgContent.getDrawable()).getBitmap();
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] byteArray = stream.toByteArray();
                                intent.putExtra("image", byteArray);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    ActivityOptions options = ActivityOptions
                                            .makeSceneTransitionAnimation((Activity) context, new Pair<View, String>(viewHodelAdmin.imgContent
                                                    , "imagetransition"));
                                    context.startActivity(intent, options.toBundle());
                                } else {
                                    context.startActivity(intent);
                                }
                            }
                        });
                        break;
                }
                viewHodelAdmin.txtTime.setText(formatTime.format(new Date(chatMessage.getCreatedate())));
                if (chatMessage.getStatus() != null && chatMessage.getStatus().equals(ChatMessage.StatusMessage.Error)) {
                    viewHodelAdmin.txtStatusMessage.setTextColor(Color.RED);
                }
                if (i == (chatMessages.size() - 1)) {
                    viewHodelAdmin.txtStatusMessage.setVisibility(View.VISIBLE);
                    switch (chatMessage.getStatus()){
                        case Error:
                            viewHodelAdmin.txtStatusMessage.setText(context.getResources().getString(R.string.error));
                            break;
                        case Sending:
                            viewHodelAdmin.txtStatusMessage.setText(context.getResources().getString(R.string.sending));
                            break;
                        case Sent:
                            viewHodelAdmin.txtStatusMessage.setText(context.getResources().getString(R.string.sent));
                            break;
                        case Received:
                            viewHodelAdmin.txtStatusMessage.setText(context.getResources().getString(R.string.recevied));
                            break;
                        case Seen:
                            viewHodelAdmin.txtStatusMessage.setText(context.getResources().getString(R.string.seen));
                            break;
                    }
                } else {
                    viewHodelAdmin.txtStatusMessage.setVisibility(View.GONE);
                }
                break;
            case CONTACT:
                Contact contact = TableContact.getInstance(context).getContact(chatMessage.getUser());
                ViewHodelContact viewHodelContact = (ViewHodelContact) viewHolder;
                if (i - 1 == -1) {
                    viewHodelContact.txtSection.setText(sectionCurrent);
                    viewHodelContact.linearLayout.setVisibility(View.VISIBLE);

                } else {
                    String sectionBefore = formatSection.format(new Date(chatMessages.get(i - 1).getCreatedate()));
                    if (sectionCurrent.equals(sectionBefore)) {
                        viewHodelContact.linearLayout.setVisibility(View.GONE);
                    } else {
                        viewHodelContact.txtSection.setText(sectionCurrent);
                        viewHodelContact.linearLayout.setVisibility(View.VISIBLE);
                    }
                }
                viewHodelContact.txtName.setText(contact.getName());
                switch (chatMessage.getTypeMessage()) {
                    case Chat:
                        viewHodelContact.txtContent.setText(chatMessages.get(i).getBody());
                        viewHodelContact.txtContent.setVisibility(View.VISIBLE);
                        viewHodelContact.imgContent.setVisibility(View.GONE);
                        break;
                    case Image:
                        viewHodelContact.txtContent.setVisibility(View.GONE);
                        viewHodelContact.imgContent.setVisibility(View.VISIBLE);
                        Glide.with(context).load(chatMessage.getBody())
                                .placeholder(R.drawable.placeholderimage)
                                .error(R.drawable.placeholderimage)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .fitCenter()
                                .override(500, 500)
                                .into(viewHodelContact.imgContent);

                        viewHodelContact.imgContent.setOnClickListener(t -> {
                            if (viewHodelContact.imgContent.getDrawable() != null) {
                                Intent intent = new Intent(context, ShareImageActivity.class);
                                Bitmap bitmap = ((BitmapDrawable) viewHodelContact.imgContent.getDrawable()).getBitmap();
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] byteArray = stream.toByteArray();
                                intent.putExtra("image", byteArray);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    ActivityOptions options = ActivityOptions
                                            .makeSceneTransitionAnimation((Activity) context, new Pair<View, String>(viewHodelContact.imgContent
                                                    , "imagetransition"));
                                    context.startActivity(intent, options.toBundle());
                                } else {
                                    context.startActivity(intent);
                                }
                            }
                        });
                        break;
                }
                viewHodelContact.txtTime.setText(formatTime.format(new Date(chatMessage.getCreatedate())));
                if (i < chatMessages.size() - 1 && chatMessage.getUser() == chatMessages.get(i + 1).getUser()) {
                    viewHodelContact.imgAvatar.setVisibility(View.INVISIBLE);
                } else {
                    viewHodelContact.imgAvatar.setVisibility(View.VISIBLE);
                    Picasso.get()
                            .load(contact.getUrlavatar())
                            .placeholder(R.drawable.account_icon)
                            .error(R.drawable.account_icon)
                            .into(viewHodelContact.imgAvatar);
                }
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getUser() == admin.getId()) {
            return ADMIN;
        }
        return CONTACT;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ViewHodelContact extends RecyclerView.ViewHolder {

        private CircleImage imgAvatar;
        private ImageView imgContent;
        private TextView txtName, txtContent, txtTime, txtSection;
        private LinearLayout linearLayout;

        public ViewHodelContact(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            imgContent = itemView.findViewById(R.id.img_content_image);
            txtName = itemView.findViewById(R.id.textview_name);
            txtContent = itemView.findViewById(R.id.textview_content);
            txtTime = itemView.findViewById(R.id.textview_time);
            txtSection = itemView.findViewById(R.id.text_view_setion);
            linearLayout = itemView.findViewById(R.id.linear_section);
        }
    }

    class ViewHodelAdmin extends RecyclerView.ViewHolder {
        private TextView txtContent, txtTime, txtStatusMessage, txtSection;
        private LinearLayout linearLayout;
        private ImageView imgContent;

        public ViewHodelAdmin(@NonNull View itemView) {
            super(itemView);
            imgContent = itemView.findViewById(R.id.img_content_image);
            txtContent = itemView.findViewById(R.id.textview_content);
            txtTime = itemView.findViewById(R.id.textview_time);
            txtStatusMessage = itemView.findViewById(R.id.textview_status_message);
            txtSection = itemView.findViewById(R.id.text_view_setion);
            linearLayout = itemView.findViewById(R.id.linear_section);
        }
    }

    public void setLoadMore(ILoadMore loadMore) {
        this.loadMore = loadMore;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public void changeStatusMessage(ChatMessage chatMessage) {
        int size = getItemCount();
        for (int i = 0; i < size; i++) {
            if (chatMessages.get(i).equals(chatMessage)) {
                chatMessages.set(i, chatMessage);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addChatMessages(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        notifyChanged();
    }

    public void notifyChanged() {
        int size = chatMessages.size();
        notifyItemInserted(size - 1);
        notifyItemChanged(size - 2);
    }
}
