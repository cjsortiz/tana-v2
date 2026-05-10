package com.tana.tana_auth.functions.registration.repository.custom;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RegistrationCustomMapper {

    @Select("SELECT userName FROM AccountMaster WHERE userName = #{userName}")
    String getAccountName(@Param("userName")String userName);
}
