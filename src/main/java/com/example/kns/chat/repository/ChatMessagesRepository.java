package com.example.kns.chat.repository;

import com.example.kns.chat.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessagesRepository {
	@Insert("INSERT INTO db.chat_messages(sender_id, group_id, content, sent) "
			+ "VALUES (#{senderId}, #{groupId}, #{content}, #{sent})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(ChatMessage message);

	@Select("""
			SELECT id, sender_id AS senderId, group_id AS groupId, content, created_at AS createdAt, sent
			FROM db.chat_messages
			WHERE sent = FALSE
			ORDER BY created_at ASC
			LIMIT #{limit}
			""")
	List<ChatMessage> findUnsentMessages(@Param("limit") int limit);

	@Update("""
			UPDATE db.chat_messages
			SET sent = TRUE
			WHERE id = #{id}
			""")
	void markAsSent(@Param("id") Long id);
}