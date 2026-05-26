package com.yuanzhang.econexus.mapper;

import com.yuanzhang.econexus.dto.DicDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 字典数据访问接口（关联查询字典类型和字典项）
 */
@Mapper
public interface DicMapper {

    /**
     * 根据字典类型编码查询所有字典项
     * @param typeCode 字典类型编码（对应 dictype.type_code）
     * @return 字典项列表（包含 typeCode、dicCode、dicLabel）
     */
    List<DicDTO> selectByTypeCode(@Param("typeCode") String typeCode);

    // 字典表相关方法（例如根据code查询区域名称等）
    String getAreaNameByCode(String areaCode);
}