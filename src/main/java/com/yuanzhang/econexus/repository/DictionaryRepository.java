package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.Dictionary;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictionaryRepository extends JpaRepository<Dictionary, String> {
    @Query("SELECT d FROM Dictionary d JOIN FETCH d.dictype WHERE d.dictype.dictypeIndex = :dictypeIndex")
    List<Dictionary> findByDictypeIndex(@Param("dictypeIndex")String dictypeIndex);
}