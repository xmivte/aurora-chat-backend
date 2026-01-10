package com.example.kns.group.repository;

import com.example.kns.group.model.Group;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
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

	@Insert("""
			INSERT INTO db.groups(id, name, image)
			VALUES (#{id}, #{name}, #{image})
			""")
	void insert(@Param("id") String id, @Param("name") String name, @Param("image") String image);
}
