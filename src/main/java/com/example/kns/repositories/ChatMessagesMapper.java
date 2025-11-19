package com.example.kns.repositories;

import com.example.kns.models.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMessagesMapper {
	@Insert("INSERT INTO chat_messages(sender_id, receiver_id, group_id, content, sent) "
			+ "VALUES (#{senderId}, #{receiverId}, #{groupId}, #{content}, #{sent})")
	void insert(ChatMessage message);


	@Select("""
			SELECT id, sender_id AS senderId, receiver_id AS receiverId,
			       group_id AS groupId, content, created_at AS createdAt, sent
			FROM chat_messages
			WHERE sent = FALSE
			ORDER BY created_at ASC
			LIMIT #{limit}
			""")
	List<ChatMessage> findUnsentMessages(@Param("limit") int limit);

	@Update("""
			UPDATE chat_messages
			SET sent = TRUE
			WHERE id = #{id}
			""")
	void markAsSent(@Param("id") Long id);
}