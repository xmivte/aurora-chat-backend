package com.example.kns.server.repository;

import com.example.kns.server.model.Server;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServerRepository {

	@Select("""
			SELECT s.id, s.name, s.user_email, s.background_color_hex
			FROM db.servers s
			JOIN db.server_groups sg
			ON s.id = sg.server_id
			JOIN db.server_group_users sgu
			ON sg.id = sgu.server_group_id
			WHERE sgu.user_email = #{userEmail}
			""")
	List<Server> findAllServersByUserId(@Param("userEmail") String userEmail);
	@Insert("""
			    INSERT INTO db.servers(name, user_email, background_color_hex)
			    VALUES (#{name}, #{userEmail}, #{backgroundColorHex})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(Server server);

	@Delete("""
			    DELETE FROM db.servers
			    WHERE id = #{serverId}
			 			AND user_email = #{userEmail}
			""")
	void deleteServer(@Param("serverId") Long serverId, @Param("userEmail") String userEmail);
}