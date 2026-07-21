package com.naenae.common.legal;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class LegalConsentValidator {

    public LocalDateTime validateTeacher(boolean termsAgreed, boolean privacyAgreed) {
        requireCommonConsents(termsAgreed, privacyAgreed);
        return LocalDateTime.now();
    }

    public LocalDateTime validateStudent(boolean termsAgreed, boolean privacyAgreed,
                                         boolean ageOrGuardianConfirmed) {
        requireCommonConsents(termsAgreed, privacyAgreed);
        if (!ageOrGuardianConfirmed) {
            throw new IllegalArgumentException("학생의 연령 또는 법정대리인 동의 여부를 확인해 주세요.");
        }
        return LocalDateTime.now();
    }

    private void requireCommonConsents(boolean termsAgreed, boolean privacyAgreed) {
        if (!termsAgreed) {
            throw new IllegalArgumentException("이용약관에 동의해 주세요.");
        }
        if (!privacyAgreed) {
            throw new IllegalArgumentException("개인정보 수집·이용에 동의해 주세요.");
        }
    }
}
