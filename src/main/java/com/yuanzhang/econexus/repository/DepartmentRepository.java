package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.Department;
import com.yuanzhang.econexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    /**
     * 查询顶级部门（parent为NULL的部门）
     */
    List<Department> findByParentDeptIsNull();

    /**
     * 根据父部门ID查询子部门
     */
    List<Department> findByParentDept_DeptIndex(String parentDeptIndex);

    /**
     * 自定义JPQL查询：递归查询所有子部门（适用于MySQL 8.0+，支持CTE）
     * 说明：若需要查询整棵树，可使用递归查询
     */
    @Query(value = "WITH RECURSIVE dept_tree AS (" +
            "    SELECT dept_index, dept_name, parent FROM t_department WHERE dept_index = :deptIndex " +
            "    UNION ALL " +
            "    SELECT d.dept_index, d.dept_name, d.parent FROM t_department d " +
            "    JOIN dept_tree dt ON d.parent = dt.dept_index" +
            ") SELECT * FROM dept_tree", nativeQuery = true)
    List<Department> findAllChildrenByDeptIndex(String deptIndex);
}