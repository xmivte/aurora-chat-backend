package com.example.kns.encryption.repository;

import com.example.kns.encryption.model.SenderKeyEnvelope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface SenderKeyEnvelopeRepository {

	@Insert("""
			  INSERT INTO db.e2ee_sender_key_envelopes
			  (chat_id, from_user_id, from_device_id, to_user_id, to_device_id, wrapped)
			  VALUES (#{chatId}, #{fromUserId}, #{fromDeviceId}, #{toUserId}, #{toDeviceId}, #{wrapped}::jsonb)
			  ON CONFLICT (chat_id, to_user_id, to_device_id)
			  DO UPDATE SET
			    from_user_id = EXCLUDED.from_user_id,
			    from_device_id = EXCLUDED.from_device_id,
			    wrapped = EXCLUDED.wrapped,
			    created_at = CURRENT_TIMESTAMP,
			    consumed_at = NULL
			""")

	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(SenderKeyEnvelope envelope);

	@Select("""
			SELECT
				id,
				chat_id AS chatId,
				from_user_id AS fromUserId,
				from_device_id AS fromDeviceId,
				to_user_id AS toUserId,
				to_device_id AS toDeviceId,
				wrapped,
				created_at AS createdAt,
				consumed_at AS consumedAt
			FROM db.e2ee_sender_key_envelopes
			WHERE chat_id = #{chatId}
			  AND to_user_id = #{toUserId}
			  AND to_device_id = #{toDeviceId}
			  AND consumed_at IS NULL
			ORDER BY created_at DESC
			LIMIT 1
			""")
	SenderKeyEnvelope findPending(@Param("chatId") String chatId, @Param("toUserId") String toUserId,
			@Param("toDeviceId") String toDeviceId);

	@Update("""
			UPDATE db.e2ee_sender_key_envelopes
			SET consumed_at = #{consumedAt}
			WHERE id = #{id}
			""")
	void consume(@Param("id") Long id, @Param("consumedAt") OffsetDateTime consumedAt);

}
