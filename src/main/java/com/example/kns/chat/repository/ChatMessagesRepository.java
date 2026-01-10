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
			SELECT m.id, m.sender_id AS senderId, m.group_id AS groupId, m.content, m.created_at AS createdAt, m.sent, u.username
			FROM db.chat_messages m
			JOIN db.users u
			ON u.id = m.sender_id
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

	@Select("""
			SELECT m.id, m.sender_id AS senderId, m.group_id AS groupId, m.content, m.created_at AS createdAt, m.sent, u.username
			FROM db.chat_messages m
			JOIN db.users u
			ON u.id = m.sender_id
			WHERE group_id = #{groupId}
			ORDER BY created_at ASC
			""")
	List<ChatMessage> findAllMessagesByGroupId(@Param("groupId") String groupId);

    @Select("""
        SELECT m.id, m.sender_id AS senderId, m.group_id AS groupId, m.content, 
               m.created_at AS createdAt, m.sent, u.username
        FROM db.chat_messages m
        JOIN db.users u ON u.id = m.sender_id
        WHERE m.id = #{messageId}
        """)
    ChatMessage findById(@Param("messageId") Long messageId);
}