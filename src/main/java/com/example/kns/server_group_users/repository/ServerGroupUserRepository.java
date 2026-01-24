package com.example.kns.server_group_users.repository;

import com.example.kns.server_group_users.model.ServerGroupUser;
import com.example.kns.server_groups.model.ServerGroup;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ServerGroupUserRepository {

	@Insert("""
			INSERT INTO db.server_group_users(server_group_id, user_id)
			VALUES (#{serverGroupId}, #{userId})
			""")
	void insert(@Param("serverGroupId") Long serverGroupId, @Param("userId") String userId);

	@Select("""
			    SELECT id, server_group_id, user_id
			    FROM db.server_group_users
			    WHERE server_group_id = #{serverGroupId}
			""")
	ServerGroupUser findByServerGroupId(@Param("serverGroupId") Long serverGroupId);
}
