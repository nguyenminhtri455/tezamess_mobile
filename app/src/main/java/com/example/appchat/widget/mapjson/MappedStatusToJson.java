package com.example.appchat.widget.mapjson;

import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedStatusToJson {
    public static Map<String, Object> mapStatusToJson(Status status) {
        Map<String, Object> map = new HashMap<>();
        map.put("userid", status.getUserid().getId());
        map.put("body", status.getBody());
        map.put("createdate", status.getCreatedate());
        if (!status.getList().isEmpty()) {
            List<Map<String, Object>> listImage = new ArrayList();
//            for (Avatar avatar : status.getList()) {
//                Map<String, Object> mapImage = new HashMap<>();
//                mapImage.put("valueBase64", avatar.getValueBase64());
//                mapImage.put("name", avatar.getName());
//                listImage.add(mapImage);
//            }
            Avatar avatar = status.getList().get(0);
            Map<String, Object> mapImage = new HashMap<>();
            mapImage.put("valueBase64", avatar.getValueBase64());
            mapImage.put("name", avatar.getName());
            listImage.add(mapImage);

            map.put("images", listImage);
        }
        return map;
    }
}
