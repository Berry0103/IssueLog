package com.yuanzhang.econexus.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pm_roler")
public class Role implements GrantedAuthority {

    @Id
    @UuidGenerator
    @Column(name = "roler_index", nullable = false)
    private String rolerIndex; // 角色唯一标识

    @Column(name = "roler_name", nullable = false, length = 64)
    private String rolerName; // 角色名称

    @Column(name = "roler_level")
    private Integer rolerlevel;

    @Override
    public String getAuthority() {
        return "ROLE_" + rolerName;
    }

}
    