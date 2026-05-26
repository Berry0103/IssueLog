package com.yuanzhang.econexus.repository;

import com.yuanzhang.econexus.model.Dictype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DictypeRepository extends JpaRepository<Dictype, String> {
    Optional<Dictype> findByDictypeName(String dictypeName);
}