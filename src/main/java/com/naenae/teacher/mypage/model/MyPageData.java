package com.naenae.teacher.mypage.model;

import java.time.LocalDateTime;

public record MyPageData(
        String name,
        String email,
        String nickname,
        boolean hasProfileImage,
        String invitationCode,
        LocalDateTime invitationCodeExpiresAt,
        int invitationCodeRemainingUses
) {
}
