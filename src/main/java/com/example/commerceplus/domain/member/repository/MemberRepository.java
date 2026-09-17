package com.example.commerceplus.domain.member.repository;

import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.entity.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
    @Query("SELECT m FROM Member m WHERE m.role IN :roleList")
    List<Member> findByRoles(@Param("roleList") List<MemberRole> roleList);
}
