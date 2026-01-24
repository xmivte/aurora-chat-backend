package com.example.kns.server.repository;

import com.example.kns.group.model.Group;
import com.example.kns.server.model.Server;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ServerRepository {

	@Select("""
			SELECT s.id, s.name, s.user_id, s.background_Color_Hex
			FROM db.servers s
			JOIN db.server_groups sg
			ON s.id = sg.server_id
			JOIN db.server_group_users sgu
			ON sg.id = sgu.server_group_id
			WHERE sgu.user_id = #{userId}
			""")
	List<Server> findAllServersByUserId(@Param("userId") String userId);
	@Insert("""
			    INSERT INTO db.servers(name, user_id, background_Color_Hex)
			    VALUES (#{name}, #{userId}, #{backgroundColorHex})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(Server server);

	@Delete("""
			    DELETE FROM db.servers
			    WHERE id = #{serverId}
			""")
	void deleteServer(@Param("serverId") Long serverId);
}