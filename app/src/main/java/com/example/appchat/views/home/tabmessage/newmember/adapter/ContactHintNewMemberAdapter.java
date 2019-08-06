package com.example.appchat.views.home.tabmessage.newmember.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.appchat.R;
import com.example.appchat.customview.CircleImage;
import com.example.appchat.objectclass.Contact;
import com.example.appchat.views.home.tabmessage.newmember.NewMemberActivity;
import com.example.appchat.widget.validate.Validator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ContactHintNewMemberAdapter extends RecyclerView.Adapter<ContactHintNewMemberAdapter.ViewHodel> implements Filterable {

    private Context context;
    private List<Contact> contactsHint;
    private List<Contact> contactListFiltered;
    private List<Contact> contactsChoose;
    private List<Contact> contactsInRoom;
    private NewMemberActivity newMemberActivity;

    public ContactHintNewMemberAdapter(Context context, List<Contact> contactsHint, List<Contact> contactsInRoom, List<Contact> contactsChoose) {
        this.context = context;
        this.contactsHint = contactsHint;
        contactListFiltered = contactsHint;
        newMemberActivity = (NewMemberActivity) context;
        this.contactsInRoom = contactsInRoom;
        this.contactsChoose = contactsChoose;
    }

    @NonNull
    @Override
    public ViewHodel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item_hint_contact, viewGroup, false);
        return new ViewHodel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHodel viewHodel, int i) {
        Contact contact = contactListFiltered.get(i);
        viewHodel.txtName.setText(contact.getName());
        if (contactsChoose.contains(contact)) {
            viewHodel.checkBox.setChecked(true);
        } else {
            viewHodel.checkBox.setChecked(false);
        }
        if (contact.getmRelationship() != 1) {
            viewHodel.txtPhone.setText(contact.getPhone());
            viewHodel.txtPhone.setVisibility(View.VISIBLE);
        } else {
            viewHodel.txtPhone.setVisibility(View.GONE);
        }
        Picasso.get()
                .load(contact.getUrlavatar())
                .placeholder(R.drawable.account_icon)
                .error(R.drawable.account_icon)
                .into(viewHodel.imgAvatar);

        if (contactsInRoom.contains(contact)) {
            viewHodel.checkBox.setChecked(true);
            viewHodel.checkBox.setEnabled(false);
            viewHodel.txtJoined.setVisibility(View.VISIBLE);
        } else {
            viewHodel.txtJoined.setVisibility(View.GONE);

            viewHodel.itemView.setOnClickListener(t -> {
                if (viewHodel.checkBox.isChecked()) {
                    viewHodel.checkBox.setChecked(false);
                    removeContactChoose(contact);
                } else {
                    viewHodel.checkBox.setChecked(true);
                    addContactChoose(contact);
                }
            });
//            viewHodel.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                if (isChecked) {
//                    addContactChoose(contact);
//                } else {
//                    removeContactChoose(contact);
//                }
//            });
        }

    }


    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString().trim().toLowerCase();
                if (charString.isEmpty()) {
                    contactListFiltered = contactsHint;
                } else {
                    charString = convertString(charString);

                    List<Contact> filteredList = new ArrayList<>();
                    for (Contact contact : contactsHint) {
                        if (Validator.checkValidatePhoneNumber(charString)) {
                            String phone = contact.getPhone();
                            if (phone.equals(charString)) {
                                filteredList.add(contact);
                                break;
                            }
                        } else {
                            String tam = convertString(contact.getName().toLowerCase());
                            if (tam.contains(charString)) {
                                filteredList.add(contact);
                            }
                        }
                    }
                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList<Contact>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    private void addContactChoose(Contact contact) {
        newMemberActivity.addContactChoose(contact);
    }

    private void removeContactChoose(Contact contact) {
        newMemberActivity.removeContactChoose(contact);
    }


    public class ViewHodel extends RecyclerView.ViewHolder {

        CircleImage imgAvatar;
        TextView txtName, txtJoined, txtPhone;
        CheckBox checkBox;

        public ViewHodel(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imageview_avatar);
            txtName = itemView.findViewById(R.id.textview_name_contact);
            txtPhone = itemView.findViewById(R.id.textview_phone_contact);
            checkBox = itemView.findViewById(R.id.checkbox_hint_contact);
            txtJoined = itemView.findViewById(R.id.textview_into_room);
        }
    }

    private char GetAlterChar(char pC) {
        char[] charA = {'à', 'á', 'ạ', 'ả', 'ã',// 0-&gt;16
                'â', 'ầ', 'ấ', 'ậ', 'ẩ', 'ẫ', 'ă', 'ằ', 'ắ', 'ặ', 'ẳ', 'ẵ'};// a,// ă,// â
        char[] charE = {'ê', 'ề', 'ế', 'ệ', 'ể', 'ễ',// 17-&gt;27
                'è', 'é', 'ẹ', 'ẻ', 'ẽ'};// e
        char[] charI = {'ì', 'í', 'ị', 'ỉ', 'ĩ'};// i 28-&gt;32
        char[] charO = {'ò', 'ó', 'ọ', 'ỏ', 'õ',// o 33-&gt;49
                'ô', 'ồ', 'ố', 'ộ', 'ổ', 'ỗ',// ô
                'ơ', 'ờ', 'ớ', 'ợ', 'ở', 'ỡ'};// ơ
        char[] charU = {'ù', 'ú', 'ụ', 'ủ', 'ũ',// u 50-&gt;60
                'ư', 'ừ', 'ứ', 'ự', 'ử', 'ữ'};// ư
        char[] charY = {'ỳ', 'ý', 'ỵ', 'ỷ', 'ỹ'};// y 61-&gt;65
        char[] charD = {'đ', ' '}; // 66-67

        String charact = String.valueOf(charA, 0, charA.length)
                + String.valueOf(charE, 0, charE.length)
                + String.valueOf(charI, 0, charI.length)
                + String.valueOf(charO, 0, charO.length)
                + String.valueOf(charU, 0, charU.length)
                + String.valueOf(charY, 0, charY.length)
                + String.valueOf(charD, 0, charD.length);

        if ((int) pC == 32) {
            return ' ';
        }

        char tam = pC;// Character.toLowerCase(pC);
        int i = 0;
        while (i < charact.length() && charact.charAt(i) != tam) {
            i++;
        }
        if (i < 0 || i > 67)
            return pC;

        if (i == 66) {
            return 'd';
        }
        if (i >= 0 && i <= 16) {
            return 'a';
        }
        if (i >= 17 && i <= 27) {
            return 'e';
        }
        if (i >= 28 && i <= 32) {
            return 'i';
        }
        if (i >= 33 && i <= 49) {
            return 'o';
        }
        if (i >= 50 && i <= 60) {
            return 'u';
        }
        if (i >= 61 && i <= 65) {
            return 'y';
        }
        return pC;
    }

    private String convertString(String pStr) {
        String convertString = pStr.toLowerCase();
        Character[] returnString = new Character[convertString.length()];
        for (int i = 0; i < convertString.length(); i++) {
            char temp = convertString.charAt(i);
            if ((int) temp < 97 || temp > 122) {
                char tam1 = GetAlterChar(temp);
                if ((int) temp != 32)
                    convertString = convertString.replace(temp, tam1);
            }
        }
        return convertString;
    }
}
