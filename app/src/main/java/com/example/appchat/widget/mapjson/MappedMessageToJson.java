package com.example.appchat.widget.mapjson;

import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MappedMessageToJson {

    public static String mapTo7Value(ChatMessage chatMessage) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("createdate", chatMessage.getCreatedate());
            if (chatMessage.getFile() != null && chatMessage.getFile() instanceof Avatar) {
                Avatar avatar = (Avatar) chatMessage.getFile();
                JSONObject mapAvatar = new JSONObject();
                mapAvatar.put("valueBase64", avatar.getValueBase64());
                mapAvatar.put("name", avatar.getName());
                jsonObject.put("body", mapAvatar.toString());
            } else {
                jsonObject.put("body", chatMessage.getBody());
            }
            jsonObject.put("room", chatMessage.getRoom());
            jsonObject.put("user", chatMessage.getUser());
            jsonObject.put("status", chatMessage.getStatus().name());
            jsonObject.put("type", chatMessage.getTypeMessage().name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String mapTo8Value(ChatMessage chatMessage, int Receiver) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", chatMessage.getId());
            jsonObject.put("createdate", chatMessage.getCreatedate());
            jsonObject.put("body", chatMessage.getBody());
            jsonObject.put("room", chatMessage.getRoom());
            jsonObject.put("user", chatMessage.getUser());
            jsonObject.put("status", chatMessage.getStatus().name());
            jsonObject.put("type", chatMessage.getTypeMessage().name());
            jsonObject.put("receiver", Receiver);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    public static String mapToOnlineOrOffLine(ChatMessage chatMessage) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("createdate", chatMessage.getCreatedate());
            jsonObject.put("body", chatMessage.getBody());
            jsonObject.put("room", chatMessage.getRoom());
            jsonObject.put("user", chatMessage.getUser());
            jsonObject.put("type", chatMessage.getTypeMessage().name());
            jsonObject.put("status", chatMessage.getStatus().name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}
