package com.naenae.common.board.controller;

import com.naenae.common.board.model.*;
import com.naenae.common.board.service.BoardService;
import com.naenae.common.file.FileDownloadResponseFactory;
import com.naenae.common.user.domain.Role;
import com.naenae.common.user.domain.User;
import com.naenae.teacher.auth.security.CustomUserDetails;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BoardController {
    private final BoardService boardService;
    private final FileDownloadResponseFactory downloadFactory;

    public BoardController(BoardService boardService, FileDownloadResponseFactory downloadFactory) {
        this.boardService = boardService; this.downloadFactory = downloadFactory;
    }

    @GetMapping({"/teacher/board", "/student/board"})
    public String list(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        User user = user(authentication); common(model, user);
        model.addAttribute("boardPage", boardService.getPosts(user.getId(), page));
        return "board/list";
    }

    @GetMapping({"/teacher/board/new", "/student/board/new"})
    public String createForm(Authentication authentication, Model model) {
        User user = user(authentication); common(model, user);
        form(model, null, "", "", List.of());
        return "board/form";
    }

    @PostMapping({"/teacher/board", "/student/board"})
    public String create(@RequestParam String title, @RequestParam String contentHtml,
                         @RequestParam(required = false) List<MultipartFile> attachments,
                         Authentication authentication, Model model, RedirectAttributes attributes) {
        User user = user(authentication); String base = base(user);
        try {
            Long id = boardService.create(user.getId(), title, contentHtml, attachments);
            attributes.addFlashAttribute("successMessage", "게시글을 등록했습니다.");
            return "redirect:" + base + "/" + id;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            common(model, user); form(model, null, title, contentHtml, List.of());
            model.addAttribute("errorMessage", exception.getMessage()); return "board/form";
        }
    }

    @GetMapping({"/teacher/board/{postId}", "/student/board/{postId}"})
    public String detail(@PathVariable Long postId, Authentication authentication, Model model) {
        User user = user(authentication); common(model, user);
        model.addAttribute("post", boardService.getPost(user.getId(), postId));
        return "board/detail";
    }

    @GetMapping({"/teacher/board/{postId}/edit", "/student/board/{postId}/edit"})
    public String editForm(@PathVariable Long postId, Authentication authentication, Model model) {
        User user = user(authentication); BoardFormData data = boardService.getForm(user.getId(), postId);
        common(model, user); form(model, data.id(), data.title(), data.contentHtml(), data.attachments());
        return "board/form";
    }

    @PostMapping({"/teacher/board/{postId}", "/student/board/{postId}"})
    public String update(@PathVariable Long postId, @RequestParam String title, @RequestParam String contentHtml,
                         @RequestParam(required = false) List<MultipartFile> attachments,
                         Authentication authentication, Model model, RedirectAttributes attributes) {
        User user = user(authentication); String base = base(user);
        try {
            boardService.update(user.getId(), postId, title, contentHtml, attachments);
            attributes.addFlashAttribute("successMessage", "게시글을 수정했습니다.");
            return "redirect:" + base + "/" + postId;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            BoardFormData existing = boardService.getForm(user.getId(), postId);
            common(model, user); form(model, postId, title, contentHtml, existing.attachments());
            model.addAttribute("errorMessage", exception.getMessage()); return "board/form";
        }
    }

    @PostMapping({"/teacher/board/{postId}/delete", "/student/board/{postId}/delete"})
    public String delete(@PathVariable Long postId, Authentication authentication, RedirectAttributes attributes) {
        User user = user(authentication); boardService.delete(user.getId(), postId);
        attributes.addFlashAttribute("successMessage", "게시글을 삭제했습니다.");
        return "redirect:" + base(user);
    }

    @PostMapping({"/teacher/board/{postId}/comments", "/student/board/{postId}/comments"})
    public String addComment(@PathVariable Long postId, @RequestParam String content,
                             Authentication authentication, RedirectAttributes attributes) {
        User user = user(authentication);
        try { boardService.addComment(user.getId(), postId, content); }
        catch (IllegalArgumentException exception) { attributes.addFlashAttribute("commentError", exception.getMessage()); }
        return "redirect:" + base(user) + "/" + postId;
    }

    @PostMapping({"/teacher/board/{postId}/comments/{commentId}/delete", "/student/board/{postId}/comments/{commentId}/delete"})
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId,
                                Authentication authentication, RedirectAttributes attributes) {
        User user = user(authentication); boardService.deleteComment(user.getId(), postId, commentId);
        attributes.addFlashAttribute("successMessage", "댓글을 삭제했습니다.");
        return "redirect:" + base(user) + "/" + postId;
    }

    @GetMapping({"/teacher/board/{postId}/attachments/{attachmentId}/download",
                 "/student/board/{postId}/attachments/{attachmentId}/download"})
    public ResponseEntity<Resource> download(@PathVariable Long postId, @PathVariable Long attachmentId,
                                             Authentication authentication) {
        BoardDownload download = boardService.download(user(authentication).getId(), postId, attachmentId);
        return downloadFactory.create(download.path(), download.originalName(), download.contentType());
    }

    private void common(Model model, User user) {
        model.addAttribute("teacherView", user.getRole() == Role.TEACHER);
        model.addAttribute("basePath", base(user));
    }
    private void form(Model model, Long id, String title, String html, List<?> attachments) {
        model.addAttribute("postId", id); model.addAttribute("editMode", id != null);
        model.addAttribute("title", title); model.addAttribute("contentHtml", html);
        model.addAttribute("existingAttachments", attachments);
    }
    private String base(User user) { return user.getRole() == Role.TEACHER ? "/teacher/board" : "/student/board"; }
    private User user(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) return details.getUser();
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
}