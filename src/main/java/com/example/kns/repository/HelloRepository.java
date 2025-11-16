package com.example.kns.repository;

import com.example.kns.entities.mockUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface HelloRepository
{
    @Select("SELECT * FROM mock_users")
    List<mockUser> findAll();

    @Select("SELECT * FROM mock_users WHERE id = #{id}")
    mockUser findById(Long id);

    @Insert("INSERT INTO mock_users (username, email) VALUES (#{username}, #{email})")
    void save(mockUser user);

    @Delete("DELETE from mock_users WHERE id = #{id}")
    void delete(Long id);
}