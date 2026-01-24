package com.example.kns.group.repository;

import com.example.kns.group.dto.ServerGroupUserRow;
import com.example.kns.group.model.Group;
import com.example.kns.group.dto.GroupUserRow;
import org.apache.ibatis.annotations.*;

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

	@Select("""
			SELECT
			g.id AS groupId,
			g.name AS groupName,
			g.image AS groupImage,
			sg.server_id AS severId,
			u.id AS userId,
			u.username AS username,
			u.image AS userImage
			FROM db.groups g
			JOIN db.server_groups sg ON sg.group_id = g.id
			         JOIN db.server_group_users sgu_me ON sgu_me.server_group_id = sg.id
			    		JOIN db.server_group_users sgu_all ON sgu_all.server_group_id= sg.id
			        	JOIN db.users u ON u.id = sgu_all.user_id
					WHERE sgu_me.user_Id = #{userId}
			""")
	List<ServerGroupUserRow> findServerGroupsWithUsers(@Param("userId") String userId);

	@Insert("""
			INSERT INTO db.groups(id, name, image)
			VALUES (#{id}, #{name}, #{image})
			""")
	void insert(@Param("id") String id, @Param("name") String name, @Param("image") String image);

	@Delete("""
			   		DELETE FROM db.groups g
			   		USING db.server_groups sg
			   		WHERE sg.group_id = g.id
			     		AND sg.server_id = #{serverId}
			""")
	void deleteServerGroups(@Param("serverId") Long serverId);

}
