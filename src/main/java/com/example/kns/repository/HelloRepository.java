package com.example.kns.repository;

import com.example.kns.entities.MockUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface HelloRepository {
	@Select("SELECT id, username, email FROM db.mock_users")
	List<MockUser> findAll();

	@Select("SELECT * FROM db.mock_users WHERE id = #{id}")
	MockUser findById(Long id);

	@Insert("INSERT INTO db.mock_users (username, email) VALUES (#{username}, #{email})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void save(MockUser user);

	@Delete("DELETE from db.mock_users WHERE id = #{id}")
	void delete(Long id);
}