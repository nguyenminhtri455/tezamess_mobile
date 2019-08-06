package com.example.appchat.views.home.tabroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.database.TableContact;
import com.example.appchat.objectclass.ChatMessage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.objectclass.Member;
import com.example.appchat.objectclass.Room;
import com.example.appchat.views.home.tabmessage.roomchat.ManyRoomActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ManyRoomAdapter extends RecyclerView.Adapter<ManyRoomAdapter.ViewHodel> {
    private List<Room> mListRoom;
    private Context mContext;
    private SimpleDateFormat format;
    private SimpleDateFormat formatDay;

    public ManyRoomAdapter(List<Room> mListRoom, Context mContext) {
        this.mListRoom = mListRoom;
        this.mContext = mContext;
        format = new SimpleDateFormat("HH:mm");
        formatDay = new SimpleDateFormat("dd/MM/yyyy");
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_item_conversation, viewGroup, false);
        ViewHodel viewHodel = new ViewHodel(view);
        return viewHodel;
    }

    @Override
    public void onBindViewHolder(@NonNull ManyRoomAdapter.ViewHodel viewHodel, int i) {
        Room room = mListRoom.get(i);
        //so luong tin nhan chua doc trong phong
        int quantity = room.getQuantityUnreadMessage();
        //tin nhan cuoi cung trong phong
        ChatMessage lastChatMessage = room.getLastChatMessage();

        viewHodel.txtName.setText(room.getName());
        if (!room.getUrlAvatar().equals("null")) {
            Glide.with(mContext)
                    .load(room.getUrlAvatar())
                    .placeholder(R.drawable.group)
                    .error(R.drawable.group)
                    .into(viewHodel.imgAvatar);
        } else {
            viewHodel.imgAvatar.setImageResource(R.drawable.group);
        }
        if (lastChatMessage != null) {
            // user nhan tin cuoi cung
            int index = room.getContacts().indexOf(new Contact(lastChatMessage.getUser()));
            Contact contact1;
            if (index != -1) {
                contact1 = room.getContacts().get(index);
            } else {
                contact1 = TableContact.getInstance(mContext).getContact(lastChatMessage.getUser());
            }
            switch (lastChatMessage.getTypeMessage()) {
                case Notify:
                    if (contact1.getId() != Member.getInstance(mContext).getId()) {
                        viewHodel.txtLastMessage.setText(contact1.getName() + lastChatMessage.getBody());
                    } else {
                        viewHodel.txtLastMessage.setText(mContext.getResources().getString(R.string.you)
                                + lastChatMessage.getBody());
                    }
                    break;
                case Chat:
                    if (contact1.getId() != Member.getInstance(mContext).getId()) {
                        viewHodel.txtLastMessage.setText(contact1.getName() + ": " + lastChatMessage.getBody());
                    } else {
                        viewHodel.txtLastMessage.setText(mContext.getResources().getString(R.string.you)
                                + ": " + lastChatMessage.getBody());
                    }
                    break;
                case Image:
                    if (contact1.getId() != Member.getInstance(mContext).getId()) {
                        viewHodel.txtLastMessage.setText(contact1.getName() + ": " + mContext.getResources().getString(R.string.image));
                    } else {
                        viewHodel.txtLastMessage.setText(mContext.getResources().getString(R.string.you)
                                + ": " + mContext.getResources().getString(R.string.image));
                    }
                    break;
            }
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH);
            int currentDay = now.get(Calendar.DAY_OF_MONTH);
            int currentHour = now.get(Calendar.HOUR_OF_DAY);

            Date date = new Date(lastChatMessage.getCreatedate());
            Calendar now2 = Calendar.getInstance();
            now2.setTime(date);
            int year = now2.get(Calendar.YEAR);
            int month = now2.get(Calendar.MONTH);
            int day = now2.get(Calendar.DAY_OF_MONTH);
            int hour = now2.get(Calendar.HOUR_OF_DAY);

            if (currentYear == year) {
                if (currentMonth == month) {
                    if (currentDay == day) {
                        if (currentHour == hour) {
                            viewHodel.txtTime.setText(format.format(date));
                        } else {
                            viewHodel.txtTime.setText((currentHour - hour)
                                    + " "
                                    + mContext.getResources().getString(R.string.hours_ago));
                        }
                    } else {
                        if (currentDay - day >= 5) {
                            viewHodel.txtTime.setText(formatDay.format(date));
                        } else {
                            if (currentDay - day == 1) {
                                viewHodel.txtTime.setText(mContext.getResources().getString(R.string.yesterday));
                            } else {
                                viewHodel.txtTime.setText((currentDay - day)
                                        + " "
                                        + mContext.getResources().getString(R.string.day_ago));
                            }
                        }
                    }
                } else {
                    if (currentMonth - month > 1) {
                        viewHodel.txtTime.setText(formatDay.format(date));
                    } else {
                        int dayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
                        int distanceDay = dayOfMonth - day + currentDay;
                        viewHodel.txtTime.setText(distanceDay
                                + " "
                                + mContext.getResources().getString(R.string.day_ago));
                    }
                }
            } else {
                viewHodel.txtTime.setText(formatDay.format(date));
            }
        } else {
            viewHodel.txtTime.setText("");
            viewHodel.txtLastMessage.setText("");
        }


        if (quantity > 0 && quantity <= 5) {
            viewHodel.txtNotify.setText(String.valueOf(quantity));
            viewHodel.txtNotify.setVisibility(View.VISIBLE);
        } else if (quantity > 5) {
            viewHodel.txtNotify.setText("5+");
            viewHodel.txtNotify.setVisibility(View.VISIBLE);
        } else if (quantity <= 0) {
            viewHodel.txtNotify.setVisibility(View.GONE);
        }

        viewHodel.itemView.setOnClickListener(t -> {
            Intent intentRoom = new Intent(mContext, ManyRoomActivity.class);
            intentRoom.putExtra("room", room);
            mContext.startActivity(intentRoom);
        });

    }

    @Override
    public int getItemCount() {
        return mListRoom.size();
    }

    public void notifyChangeLastChatMessageInRoom(int idRoom) {
        int size = getItemCount();
        if (mListRoom.get(0).getId() == idRoom) {
            notifyItemChanged(0);
        } else {
            for (int i = 0; i < size; i++) {
                Room room = mListRoom.get(i);
                if (room.getId() == idRoom) {
                    mListRoom.remove(i);
                    mListRoom.add(0, room);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    public void notifyResetQuantityUnReadMessageRoom(int idRoom) {
        int size = getItemCount();
        for (int i = 0; i < size; i++) {
            Room room = mListRoom.get(i);
            if (room.getId() == idRoom) {
                notifyItemChanged(i);
                return;
            }

        }
    }

    class ViewHodel extends RecyclerView.ViewHolder {

        CircleImage imgAvatar;
        TextView txtName, txtLastMessage, txtTime, txtNotify;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name);
            txtLastMessage = itemView.findViewById(R.id.textview_last_message);
            txtTime = itemView.findViewById(R.id.textview_time);
            txtNotify = itemView.findViewById(R.id.textview_notify);
        }
    }
}


