package com.platform.studiotranslator.entity;

import com.platform.studiotranslator.constant.Language;
import com.platform.studiotranslator.constant.ProficiencyLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageSkill {

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", length = 5, nullable = false)
    private Language code;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false)
    private ProficiencyLevel level;
}
