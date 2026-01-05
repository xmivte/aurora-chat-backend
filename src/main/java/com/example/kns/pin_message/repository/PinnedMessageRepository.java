package com.example.kns.pin_message.repository;

import com.example.kns.pin_message.model.PinnedMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PinnedMessageRepository {

	@Insert("""
			INSERT INTO db.pinned_messages (message_id, group_id, pinned_by)
			VALUES (#{messageId}, #{groupId}, #{pinnedBy})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(PinnedMessage pinnedMessage);

	@Delete("""
			DELETE FROM db.pinned_messages
			WHERE message_id = #{messageId}
			  AND group_id = #{groupId}
			""")
	void delete(@Param("messageId") Long messageId, @Param("groupId") String groupId);

	@Select("""
			SELECT
			    id,
			    message_id AS messageId,
			    group_id AS groupId,
			    pinned_by AS pinnedBy,
			    pinned_at AS pinnedAt
			FROM db.pinned_messages
			WHERE group_id = #{groupId}
			ORDER BY pinned_at DESC
			""")
	List<PinnedMessage> findByGroupId(@Param("groupId") String groupId);

	@Select("""
			SELECT COUNT(*)
			FROM db.pinned_messages
			WHERE group_id = #{groupId}
			""")
	int countByGroupId(@Param("groupId") String groupId);

	@Select("""
			SELECT
			    id,
			    message_id AS messageId,
			    group_id AS groupId,
			    pinned_by AS pinnedBy,
			    pinned_at AS pinnedAt
			FROM db.pinned_messages
			WHERE message_id = #{messageId}
			  AND group_id = #{groupId}
			LIMIT 1
			""")
	PinnedMessage findOne(@Param("messageId") Long messageId, @Param("groupId") String groupId);
}