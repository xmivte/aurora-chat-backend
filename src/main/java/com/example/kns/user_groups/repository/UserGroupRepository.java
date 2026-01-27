package com.example.kns.user_groups.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserGroupRepository {

	@Insert("""
			<script>
			    INSERT INTO db.user_groups(user_id, group_id, last_read_at, unread_count)
			    VALUES
			    <foreach collection="userIds" item="userId" separator=",">
			        (#{userId}, #{groupId}, CURRENT_TIMESTAMP, 0)
			    </foreach>
			</script>
			""")
	void insertMany(@Param("userIds") List<String> userIds, @Param("groupId") String groupId);

	@Select("""
			SELECT user_id
			FROM db.user_groups
			WHERE group_id = #{groupId}
			""")
	List<String> findUserIdsByGroupId(@Param("groupId") String groupId);

	@Delete("DELETE FROM db.user_groups WHERE user_id = #{userId}")
	void deleteByUserId(@Param("userId") String userId);
}