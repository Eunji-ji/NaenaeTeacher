package com.naenae.template;

import static org.assertj.core.api.Assertions.assertThat;
import com.naenae.common.board.model.*;
import com.naenae.common.pagination.PageView;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

class BoardTemplateTest {
    private SpringTemplateEngine engine; private IWebExchange exchange;
    @BeforeEach void setUp() {
        var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setTemplateMode("HTML");resolver.setCharacterEncoding("UTF-8");
        engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);var context=new MockServletContext();
        exchange=JakartaServletWebApplication.buildApplication(context).buildExchange(new MockHttpServletRequest(context),new MockHttpServletResponse());
    }
    @Test void boardListRendersWebsiteColumnsCountsAndPagination() {
        var values=base(true,"/teacher/board");values.put("boardPage",new PageView<>(List.of(new BoardListItem(1L,"질문 있어요","[중등 A반] 김학생",LocalDateTime.of(2026,7,15,9,30),12,3,1)),0,2,11,true,false,List.of(0,1)));
        String html=render("board/list",values);
        assertThat(html).contains("제목","작성자","작성일시","조회수","[중등 A반] 김학생","[3]","/teacher/board?page=1");
    }
    @Test void boardFormAndDetailRenderFilesAndCommentsForStudent() {
        var form=base(false,"/student/board");form.put("editMode",true);form.put("postId",1L);form.put("title","질문");form.put("contentHtml","<p>본문</p>");form.put("existingAttachments",List.of(new BoardAttachmentItem(2L,"자료.pdf","10 KB")));
        assertThat(render("board/form",form)).contains("게시글 수정","자료.pdf","/student/board/1/attachments/2/download");
        var detail=base(false,"/student/board");detail.put("post",new BoardDetail(1L,"질문","[중등 A반] 김학생",LocalDateTime.of(2026,7,15,9,30),13,"<p>본문</p>",List.of(new BoardAttachmentItem(2L,"자료.pdf","10 KB")),List.of(new BoardCommentItem(3L,"[선생님] 김선생","확인했어요",LocalDateTime.of(2026,7,15,10,0),false)),true,true));
        String html=render("board/detail",detail);assertThat(html).contains("조회 13","댓글","확인했어요","/student/board/1/comments","/student/board/1/edit");
    }
    private HashMap<String,Object> base(boolean teacher,String path){var v=new HashMap<String,Object>();v.put("teacherView",teacher);v.put("basePath",path);v.put("teacherDisplayName","테스트 선생님");v.put("_csrf",new DefaultCsrfToken("X-CSRF-TOKEN","_csrf","token"));return v;}
    private String render(String name,HashMap<String,Object> values){return engine.process(name,new WebContext(exchange,Locale.KOREAN,values));}
}