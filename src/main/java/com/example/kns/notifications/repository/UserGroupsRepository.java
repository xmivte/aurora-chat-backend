package com.example.kns.notifications.repository;

import com.example.kns.notifications.repository.model.UnreadCountRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserGroupsRepository {

	@Update("""
			UPDATE db.user_groups
			SET unread_count = COALESCE(unread_count, 0) + 1
			WHERE group_id = #{groupId}
			  AND (#{senderId} IS NULL OR user_id <> #{senderId})
			""")
	void incrementUnreadForGroupExceptSender(@Param("groupId") String groupId, @Param("senderId") String senderId);

	@Select("""
			SELECT group_id AS groupId, COALESCE(unread_count, 0) AS unreadCount
			FROM db.user_groups
			WHERE user_id = #{userId}
			""")
	List<UnreadCountRow> findUnreadCountsByUserId(@Param("userId") String userId);

	@Select("""
			SELECT user_id AS userId, COALESCE(unread_count, 0) AS unreadCount
			FROM db.user_groups
			WHERE group_id = #{groupId}
			  AND user_id IS NOT NULL
			  AND (#{senderId} IS NULL OR user_id <> #{senderId})
			""")
	List<UnreadCountRow> findUnreadCountsForGroupExceptSender(@Param("groupId") String groupId,
			@Param("senderId") String senderId);

	@Select("SELECT MAX(id) FROM db.chat_messages WHERE group_id = #{groupId}")
	Long findLatestMessageId(@Param("groupId") String groupId);

	@Update("""
			         UPDATE db.user_groups
			         SET unread_count = 0,
			         last_read_message_id = #{lastReadMessageId}
			          WHERE user_id = #{userId}
			           AND group_id = #{groupId}
			""")
	void markGroupRead(@Param("userId") String userId, @Param("groupId") String groupId,
			@Param("lastReadMessageId") Long lastReadMessageId);
}
