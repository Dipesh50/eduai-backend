package com.eduai.eduai_backend.repository;

import com.eduai.eduai_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopic(String topic);

    @Query(value = "SELECT * FROM questions WHERE topic = :topic ORDER BY RAND() LIMIT :limit",
            nativeQuery = true)
    List<Question> findRandomByTopic(@Param("topic") String topic,
                                     @Param("limit") int limit);

    List<String> findDistinctTopicBy();
}