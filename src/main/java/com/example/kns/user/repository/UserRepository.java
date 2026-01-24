package com.example.kns.user.repository;

import com.example.kns.user.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRepository {

	@Select("""
			SELECT u.id, u.username, u.email, u.image
			FROM db.users u
			JOIN db.user_groups ug
			ON u.id = ug.user_id
			WHERE ug.group_id = #{groupId}
			""")
	List<User> findAllUsersByGroupId(@Param("groupId") String groupId);

	@Select("""
			SELECT DISTINCT u.id, u.username, u.email, u.image
			FROM db.users u
			JOIN db.server_group_users sgu
			ON u.id = sgu.user_id
			JOIN db.server_groups sg
			ON sg.id = sgu.server_group_id
			WHERE sg.server_id = #{serverId}
			""")
	List<User> findAllServer(@Param("serverId") Long serverId);

	@Select("""
			SELECT id, username, email, image
			FROM db.users
			""")
	List<User> findAllUsers();

	@Insert("""
			INSERT INTO db.users(id, username, email, image)
			VALUES (#{id}, #{username}, #{email}, #{image})
			""")
	void insert(@Param("id") String id, @Param("username") String username, @Param("email") String email,
			@Param("image") String image);
}
