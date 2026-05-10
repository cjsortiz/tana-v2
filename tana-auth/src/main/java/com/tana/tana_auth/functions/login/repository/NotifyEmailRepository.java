package com.tana.tana_auth.functions.login.repository;

import com.tana.tana_common.model.NotifyEmail;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifyEmailRepository extends JpaRepository<NotifyEmail, Long> {

    @Query(value = "SELECT * " +
        "FROM NotifyEmail " +
        "WHERE email = :email",nativeQuery = true)
    NotifyEmail findByEmail(@Param("email") String email);
}
