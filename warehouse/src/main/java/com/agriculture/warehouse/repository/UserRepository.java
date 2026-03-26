package com.agriculture.warehouse.repository;



import com.agriculture.warehouse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查询用户（用于登录验证）
    Optional<User> findByUsername(String username);
}
