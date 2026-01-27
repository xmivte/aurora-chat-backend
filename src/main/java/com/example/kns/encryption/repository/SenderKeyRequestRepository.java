package com.example.kns.encryption.repository;

import com.example.kns.encryption.model.SenderKeyRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface SenderKeyRequestRepository {

	@Insert("""
			  INSERT INTO db.e2ee_sender_key_requests
			  (chat_id, requester_user_id, requester_device_id)
			  VALUES (#{chatId}, #{requesterUserId}, #{requesterDeviceId})
			  ON CONFLICT (chat_id, requester_user_id, requester_device_id, fulfilled_at)
			  DO NOTHING
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insertIfNotExists(SenderKeyRequest request);

	@Select("""
			  SELECT
			    id,
			    chat_id AS chatId,
			    requester_user_id AS requesterUserId,
			    requester_device_id AS requesterDeviceId,
			    created_at AS createdAt,
			    fulfilled_at AS fulfilledAt
			  FROM db.e2ee_sender_key_requests
			  WHERE chat_id = #{chatId}
			    AND fulfilled_at IS NULL
			  ORDER BY created_at DESC
			""")
	List<SenderKeyRequest> findPendingByChatId(@Param("chatId") String chatId);

	@Select("""
			  SELECT
			    id,
			    chat_id AS chatId,
			    requester_user_id AS requesterUserId,
			    requester_device_id AS requesterDeviceId,
			    created_at AS createdAt,
			    fulfilled_at AS fulfilledAt
			  FROM db.e2ee_sender_key_requests
			  WHERE chat_id = #{chatId}
			    AND requester_user_id = #{userId}
			    AND requester_device_id = #{deviceId}
			    AND fulfilled_at IS NULL
			  ORDER BY created_at DESC
			  LIMIT 1
			""")
	SenderKeyRequest findPendingRequest(@Param("chatId") String chatId, @Param("userId") String userId,
			@Param("deviceId") String deviceId);

	@Update("""
			  UPDATE db.e2ee_sender_key_requests
			  SET fulfilled_at = #{fulfilledAt}
			  WHERE id = #{id}
			""")
	void markFulfilled(@Param("id") Long id, @Param("fulfilledAt") OffsetDateTime fulfilledAt);
}