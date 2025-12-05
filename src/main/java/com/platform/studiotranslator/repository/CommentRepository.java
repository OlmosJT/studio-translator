package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {
    // Mostly handled by finding by ChapterID, which JPA does automatically
    // if we use a Specification or standard Pageable query.
}
