package org.bytebound;

import com.google.gson.Gson;

import java.util.List;

public class JsonConverter {

    public static String convertChatsToJson(List<String> chats) {
        Gson gson = new Gson();
        return gson.toJson(chats);
    }

}
