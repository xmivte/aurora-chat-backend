package com.example.kns.server_groups.repository;

import com.example.kns.server_groups.model.ServerGroup;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ServerGroupsRepository {

	@Insert("""
			       INSERT INTO db.server_groups(server_id, group_id)
			       VALUES (#{serverId}, #{groupId})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(ServerGroup serverGroup);

	@Select("""
			    SELECT id, server_id, group_id
			    FROM db.server_groups
			    WHERE server_id = #{serverId}
			""")
	ServerGroup findByServerId(@Param("serverId") Long serverId);
}
