package com.example.kns.user_groups.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserGroupRepository {

    @Insert("""
        INSERT INTO db.user_groups(user_id, group_id, last_read_at, unread_count)
        VALUES (#{userId}, #{groupId}, CURRENT_TIMESTAMP, 0)
    """)
    void insert(@Param("userId") String userId, @Param("groupId") String groupId);
}