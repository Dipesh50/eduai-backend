package com.eduai.eduai_backend.repository;

import com.eduai.eduai_backend.entity.SkillGapReport;
import com.eduai.eduai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillGapRepository
        extends JpaRepository<SkillGapReport, Long> {

    List<SkillGapReport> findByUserOrderByCreatedAtDesc(User user);
}