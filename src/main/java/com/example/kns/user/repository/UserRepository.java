package com.example.kns.user.repository;

import com.example.kns.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

	@Insert("""
        INSERT INTO db.users(id, username, email, image)
        VALUES (#{id}, #{username}, #{email}, #{image})
    """)
	void insert(@Param("id") String id, @Param("username") String username, @Param("email") String email,
				@Param("image") String image);
}