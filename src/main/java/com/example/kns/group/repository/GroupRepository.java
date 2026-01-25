package com.example.kns.group.repository;

import com.example.kns.group.model.Group;
import com.example.kns.group.dto.GroupUserRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupRepository {

	@Select("""
			SELECT g.id, g.name, g.image
			FROM db.groups g
			JOIN db.user_groups ug
			ON g.id = ug.group_id
			WHERE ug.user_id = #{userId}
			""")
	List<Group> findAllGroupsByUserId(@Param("userId") String userId);

	@Select("""
			SELECT COUNT(*) > 0
			FROM db.user_groups
			WHERE user_id = #{userId} AND group_id = #{groupId}
			""")
	boolean isUserInGroup(@Param("userId") String userId, @Param("groupId") String groupId);

	@Select("""
			SELECT
			g.id AS groupId,
			g.name AS groupName,
			g.image AS groupImage,
			u.id AS userId,
			u.username AS username,
			u.image AS userImage
			FROM db.groups g
			JOIN db.user_groups ug_me ON ug_me.group_id = g.id
			JOIN db.user_groups ug_all ON ug_all.group_id = g.id
			JOIN db.users u ON u.id = ug_all.user_id
			WHERE ug_me.user_id = #{userId}
			""")
	List<GroupUserRow> findGroupsWithUsers(@Param("userId") String userId);

	@Insert("""
			INSERT INTO db.groups(id, name, image)
			VALUES (#{id}, #{name}, #{image})
			""")
	void insert(@Param("id") String id, @Param("name") String name, @Param("image") String image);
}
