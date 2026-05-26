package com.yuanzhang.econexus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

/**
 * 部门实体类（JPA自关联树形结构）
 * 适配表结构：主键dept_index，父部门字段parent（存储父部门dept_index）
 */
@Data
@Entity
@Table(name = "pm_department") // 对应数据库表名，根据实际修改
public class Department {

    /**
     * 主键：部门索引（dept_index）
     */
    @Id
    @UuidGenerator
    @Column(name = "dept_index", nullable = false) // 映射数据库字段名
    private String deptIndex;

    /**
     * 部门名称
     */
    @Column(name = "dept_name", nullable = false, length = 64)
    private String deptName;

    /**
     * 父部门字段：自关联（多对一）
     * 说明：多个子部门对应一个父部门，所以用@ManyToOne
     */
    @ManyToOne(fetch = FetchType.LAZY) // 懒加载：查询子部门时不主动加载父部门，提升性能
    @JoinColumn(
            name = "parent", // 数据库中存储父部门的字段名（parent）
            referencedColumnName = "dept_index", // 关联父部门的主键字段（dept_index）
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) // 若数据库已手动加外键，可设为NO_CONSTRAINT避免重复
    )
    private Department parentDept; // 父部门实体（而非直接存Long型parent）

    /**
     * 部门编码（可选字段）
     */
    @Column(name = "dept_code", unique = true, length = 32)
    private String deptCode;

    /**
     * 描述（中文名）
     */
    @Column(name = "description", unique = true, length = 32)
    private String description;

    /**
     * 解决JSON序列化循环引用问题（若需要返回JSON）
     * 说明：双向关联时，JSON序列化会陷入死循环，需排除一方
     */
    @JsonIgnore // 需引入jackson-databind依赖
    public Department getParentDept() {
        return parentDept;
    }

    // 若使用Lombok @Data，无需手动写getter/setter；若不使用，需补充
}