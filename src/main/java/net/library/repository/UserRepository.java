package net.library.repository;

import net.library.model.entity.User;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.moderationState = :moderationState WHERE u.id = :userId")
    int updateModerationState(@Param("userId") UUID userId, @Param("moderationState") ModerationState moderationState);

    @Modifying
    @Query("UPDATE User u SET u.userState=:userState WHERE u.id = :userId")
    int updateUserState(@Param("userId") UUID userId, @Param("userState") UserState userState);

    @Modifying
    @Query("UPDATE User u SET u.roleType=:roleType WHERE u.id = :userId")
    int updateRoleType(@Param("userId") UUID userId, @Param("roleType") RoleType roleType);
}