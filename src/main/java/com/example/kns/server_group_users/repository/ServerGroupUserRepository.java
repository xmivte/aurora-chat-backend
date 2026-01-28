package com.example.kns.server_group_users.repository;

import com.example.kns.server_group_users.model.ServerGroupUser;
import com.example.kns.server_groups.model.ServerGroup;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ServerGroupUserRepository {

	@Insert("""
			INSERT INTO db.server_group_users(server_group_id, user_email)
			VALUES (#{serverGroupId}, #{userEmail})
			""")
	void insert(@Param("serverGroupId") Long serverGroupId, @Param("userEmail") String userEmail);

	@Select("""
			    SELECT id, server_group_id, user_email
			    FROM db.server_group_users
			    WHERE server_group_id = #{serverGroupId}
			""")
	ServerGroupUser findByServerGroupId(@Param("serverGroupId") Long serverGroupId);

	@Delete("""
			   		DELETE FROM db.server_group_users sgu
			   		USING db.server_groups sg,
					       			  db.servers s
			   		WHERE sg.id = sgu.server_group_id
			     	AND sg.server_id = #{serverId}
					    		AND s.id = sg.server_id
								AND s.user_email = #{userEmail}
			""")
	void deleteServerGroupUsers(@Param("serverId") Long serverId, @Param("userEmail") String userEmail);
}
