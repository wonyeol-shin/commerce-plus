package com.example.commerceplus.domain.member.entity;

import com.example.commerceplus.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 100 )
    private String password;

    @Column(nullable = false, length = 50 )
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    public static Member createNormalMember(String email, String password, String name, String phoneNumber) {
        return new Member(email, password, name, phoneNumber, MemberRole.NORMAL, MemberStatus.ACTIVE);
    }

    public static Member createAdminMember(String email, String password, String name, String phoneNumber, MemberRole role) {
        return new Member(email, password, name, phoneNumber, role, MemberStatus.INACTIVE);
    }

    public void activeAdmin(){
        this.status = MemberStatus.ACTIVE;
    }

    public void inactiveAdmin(){
        this.status = MemberStatus.INACTIVE;
    }


    private Member(String email, String password, String name, String phoneNumber, MemberRole role, MemberStatus status) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }
}
