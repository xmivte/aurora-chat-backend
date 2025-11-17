package com.example.kns.repositories;

import com.example.kns.models.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMessagesMapper {

    @Insert("INSERT INTO chat_messages(sender, content) VALUES (#{sender}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChatMessage message);

    @Select("SELECT * FROM chat_messages ORDER BY created_at ASC LIMIT #{limit}")
    List<ChatMessage> findLastMessages(@Param("limit") int limit);
}