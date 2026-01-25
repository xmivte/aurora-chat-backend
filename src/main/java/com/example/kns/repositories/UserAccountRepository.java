package com.example.kns.repositories;

import com.example.kns.dto.UserContext;
import com.example.kns.dto.UserDataDto;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
@Repository
public interface UserAccountRepository {
	@Select("SELECT username, image FROM db.users WHERE email = #{email}")
	Optional<UserDataDto> findByUserId(UserContext userContext);

	@Insert("INSERT INTO db.users(id, email, username, image) VALUES (#{userCtx.email}, #{userCtx.email}, #{userData.username}, #{userData.image})")
	void save(@Param("userCtx") UserContext userContext, @Param("userData") UserDataDto usersDataDto);

	@Select("SELECT COUNT(id) FROM db.users WHERE username = #{uniqueUsername}")
	int countUsersByUsername(String uniqueUsername);

	@Delete("DELETE FROM db.users WHERE email = #{userCtx.email}")
	void delete(@Param("userCtx") UserContext userContext);

	@Update("UPDATE db.users SET username = #{newUsername} WHERE email = #{userCtx.email}")
	void updateUsername(@Param("userCtx") UserContext userContext, String newUsername);
}