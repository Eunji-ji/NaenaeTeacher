package com.naenae.teacher.invitation.service;

import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.teacher.profile.repository.TeacherRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvitationCodeService {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 24;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final TeacherRepository teacherRepository;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final int validDays;
    private final int maxUses;

    @Autowired
    public InvitationCodeService(
            TeacherRepository teacherRepository,
            @Value("${app.security.invitation-code.valid-days:30}") int validDays,
            @Value("${app.security.invitation-code.max-uses:100}") int maxUses
    ) {
        this(teacherRepository, new SecureRandom(), Clock.systemDefaultZone(), validDays, maxUses);
    }

    InvitationCodeService(TeacherRepository teacherRepository, SecureRandom secureRandom, Clock clock,
                          int validDays, int maxUses) {
        if (validDays < 1 || maxUses < 1) {
            throw new IllegalArgumentException("초대코드 유효기간과 사용 횟수는 1 이상이어야 합니다.");
        }
        this.teacherRepository = teacherRepository;
        this.secureRandom = secureRandom;
        this.clock = clock;
        this.validDays = validDays;
        this.maxUses = maxUses;
    }

    public void ensureActive(Teacher teacher) {
        if (!teacher.canAcceptInvitation(now())) {
            issue(teacher);
        }
    }

    public void reissue(Teacher teacher) {
        issue(teacher);
    }

    public Teacher requireActive(String rawCode) {
        String code = normalize(rawCode);
        Teacher teacher = teacherRepository.findByInvitationCode(code)
                .orElseThrow(this::invalidCode);
        if (!teacher.canAcceptInvitation(now())) {
            throw invalidCode();
        }
        return teacher;
    }

    public void consume(Teacher teacher, String rawCode) {
        String code = normalize(rawCode);
        if (teacher.getId() == null
                || teacherRepository.consumeInvitation(teacher.getId(), code, now()) != 1) {
            throw invalidCode();
        }
    }

    public String normalize(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        if (!code.matches("[A-HJ-NP-Z2-9]{24}")) {
            throw invalidCode();
        }
        return code;
    }

    private void issue(Teacher teacher) {
        teacher.issueInvitationCode(uniqueCode(), now().plusDays(validDays), maxUses);
    }

    private String uniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int index = 0; index < CODE_LENGTH; index++) {
                code.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
            }
            String value = code.toString();
            if (!teacherRepository.existsByInvitationCode(value)) {
                return value;
            }
        }
        throw new IllegalStateException("초대코드를 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private IllegalArgumentException invalidCode() {
        return new IllegalArgumentException("유효하지 않거나 만료된 초대코드입니다. 선생님께 새 코드를 요청해 주세요.");
    }
}
