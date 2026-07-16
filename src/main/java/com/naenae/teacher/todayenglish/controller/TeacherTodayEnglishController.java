package com.naenae.teacher.todayenglish.controller;

import com.naenae.common.user.domain.User;
import com.naenae.common.vocabulary.domain.WordLevel;
import com.naenae.teacher.auth.security.CustomUserDetails;
import com.naenae.teacher.todayenglish.model.TodayEnglishWordCounts;
import com.naenae.teacher.todayenglish.service.TeacherTodayEnglishService;
import com.naenae.teacher.todayenglish.service.TeacherTodayEnglishSentenceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/today-english")
public class TeacherTodayEnglishController {
    private final TeacherTodayEnglishService todayEnglishService;
    private final TeacherTodayEnglishSentenceService sentenceService;

    public TeacherTodayEnglishController(TeacherTodayEnglishService todayEnglishService,
                                         TeacherTodayEnglishSentenceService sentenceService) {
        this.todayEnglishService = todayEnglishService;
        this.sentenceService = sentenceService;
    }

    @GetMapping
    public String page(Authentication authentication, Model model) {
        Long userId = userId(authentication);
        model.addAttribute("wordCounts", todayEnglishService.getCounts(userId));
        return "teacher/today-english";
    }

    @GetMapping("/words")
    public String words(@RequestParam(required = false) WordLevel level,
                        @RequestParam(defaultValue = "0") int page,
                        Authentication authentication, Model model) {
        Long userId = userId(authentication);
        model.addAttribute("wordPage", todayEnglishService.getWords(userId, level, page));
        model.addAttribute("wordLevels", WordLevel.values());
        model.addAttribute("selectedLevel", level);
        model.addAttribute("wordPaginationUrl", level == null
                ? "/teacher/today-english/words"
                : "/teacher/today-english/words?level=" + level.name());
        return "teacher/today-english-words";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template(Authentication authentication) {
        byte[] template = todayEnglishService.createTemplate(userId(authentication));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"today-english-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Authentication authentication,
                         RedirectAttributes redirectAttributes, Model model) {
        Long userId = userId(authentication);
        try {
            TodayEnglishWordCounts counts = todayEnglishService.replaceWords(userId, file);
            redirectAttributes.addFlashAttribute("successMessage",
                    "오늘의 영어 단어 " + counts.total() + "개를 등록했습니다.");
            return "redirect:/teacher/today-english";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("wordCounts", todayEnglishService.getCounts(userId));
            return "teacher/today-english";
        }
    }

    @PostMapping("/words/{wordId}/delete")
    public String deleteWord(@PathVariable Long wordId,
                             @RequestParam(required = false) WordLevel level,
                             @RequestParam(defaultValue = "0") int page,
                             Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            todayEnglishService.deleteWord(userId(authentication), wordId);
            redirectAttributes.addFlashAttribute("successMessage", "단어를 삭제했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        String redirect = "redirect:/teacher/today-english/words?page=" + Math.max(page, 0);
        return level == null ? redirect : redirect + "&level=" + level.name();
    }

    @GetMapping("/sentences/upload")
    public String sentenceUploadPage(Authentication authentication, Model model) {
        model.addAttribute("sentenceCounts", sentenceService.getCounts(userId(authentication)));
        return "teacher/today-english-sentences-upload";
    }

    @GetMapping("/sentences/template")
    public ResponseEntity<byte[]> sentenceTemplate(Authentication authentication) {
        byte[] template = sentenceService.createTemplate(userId(authentication));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"today-english-sentence-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    @PostMapping("/sentences/upload")
    public String uploadSentences(@RequestParam("file") MultipartFile file, Authentication authentication,
                                  RedirectAttributes redirectAttributes, Model model) {
        Long userId = userId(authentication);
        try {
            var counts = sentenceService.replaceSentences(userId, file);
            redirectAttributes.addFlashAttribute("successMessage",
                    "오늘의 영어 문장 " + counts.total() + "개를 등록했습니다.");
            return "redirect:/teacher/today-english/sentences/upload";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("sentenceCounts", sentenceService.getCounts(userId));
            return "teacher/today-english-sentences-upload";
        }
    }

    @GetMapping("/sentences")
    public String sentences(@RequestParam(required = false) WordLevel level,
                            @RequestParam(defaultValue = "0") int page,
                            Authentication authentication, Model model) {
        Long userId = userId(authentication);
        model.addAttribute("sentencePage", sentenceService.getSentences(userId, level, page));
        model.addAttribute("sentenceLevels", WordLevel.values());
        model.addAttribute("selectedLevel", level);
        model.addAttribute("sentencePaginationUrl", level == null
                ? "/teacher/today-english/sentences"
                : "/teacher/today-english/sentences?level=" + level.name());
        return "teacher/today-english-sentences";
    }

    @PostMapping("/sentences/{sentenceId}/delete")
    public String deleteSentence(@PathVariable Long sentenceId,
                                 @RequestParam(required = false) WordLevel level,
                                 @RequestParam(defaultValue = "0") int page,
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            sentenceService.deleteSentence(userId(authentication), sentenceId);
            redirectAttributes.addFlashAttribute("successMessage", "문장을 삭제했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        String redirect = "redirect:/teacher/today-english/sentences?page=" + Math.max(page, 0);
        return level == null ? redirect : redirect + "&level=" + level.name();
    }

    private Long userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            User user = details.getUser();
            return user.getId();
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}
