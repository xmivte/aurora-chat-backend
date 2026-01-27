package com.example.kns.encryption.repository;

import com.example.kns.encryption.model.DeviceKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceKeyRepository {

	@Insert("""
			  INSERT INTO db.e2ee_devices (device_id)
			  VALUES (#{deviceId})
			  ON CONFLICT (device_id) DO NOTHING
			""")
	void ensureDeviceExists(@Param("deviceId") String deviceId);

	@Insert("""
			  INSERT INTO db.e2ee_user_devices (user_id, device_id, identity_key_public)
			  VALUES (#{userId}, #{deviceId}, #{identityKeyPublic}::jsonb)
			  ON CONFLICT (user_id, device_id)
			  DO UPDATE SET
			    identity_key_public = EXCLUDED.identity_key_public,
			    updated_at = CURRENT_TIMESTAMP
			""")
	void upsert(DeviceKey deviceKey);

	@Select("""
			  SELECT
			    device_id AS deviceId,
			    user_id AS userId,
			    identity_key_public AS identityKeyPublic
			  FROM db.e2ee_user_devices
			  WHERE user_id = #{userId}
			""")
	List<DeviceKey> findByUserId(@Param("userId") String userId);

}
