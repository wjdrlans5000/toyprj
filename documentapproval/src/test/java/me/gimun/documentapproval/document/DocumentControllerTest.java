package me.gimun.documentapproval.document;


import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.accounts.AccountRepository;
import me.gimun.documentapproval.auth.model.UserAccount;
import me.gimun.documentapproval.common.BaseControllerTest;
import me.gimun.documentapproval.common.TestDescription;
import me.gimun.documentapproval.config.WithMockCustomUser;
import me.gimun.documentapproval.document.dto.DocumentRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class DocumentControllerTest extends BaseControllerTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Test
    @WithMockCustomUser(username = "username1", roles = "USER")
    @TestDescription("30개의 문서를 10개씩 두번째 페이지 조회하기")
    public void getDocuments() throws Exception {
        //Given
        //로그인한 사용자 id
        Integer userId = getLoginId();

        List<Integer> approvalIds = new ArrayList<>();
        approvalIds.add(userId);
        IntStream.range(0,4).forEach(i -> {
            //계정 생성
            Account accountParam = this.generateAccount(i);
            approvalIds.add(accountParam.getId());
        });
        System.out.println("approvalIds = " + approvalIds);
        IntStream.range(0,2).forEach(i -> {
            this.generateDocument(approvalIds,userId);
        });

        // 로그인한 사용자가 작성하거나 결재권자인 문서만 추출하기위하여  작성자가아니고 결재권자가아닌  샘플문서 두개 생성
        Account accountParam6= this.generateAccount(6);
        Account accountParam7 = this.generateAccount(7);
        List<Integer> approvalIds2 = new ArrayList<>();
        approvalIds2.add(accountParam6.getId());
        approvalIds2.add(accountParam7.getId());

        Document document = Document.createDocument("docutitle", Document.Category.EXPENSE,"buythings",approvalIds2,3);
        Document document2 = Document.createDocument("docutitle", Document.Category.EXPENSE,"buythings",approvalIds2,4);
        this.documentRepository.save(document);
        this.documentRepository.save(document2);
        //

        //When
        //2페이지에 해당하는 이벤트 목록을 요청
        //GET /api/events?page=1&size=10&sort=name,DESC로 요청을 보낸다.
        this.mockMvc.perform(get("/api/documents")
                .param("page","0") //패이지는 0부터 시작
                .param("size","10")
                .param("sort","id,DESC")
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.document-create").exists())
                .andExpect(jsonPath("_embedded.documentResponseList[0]._links").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("document-list"
                        ,requestParameters(
                                parameterWithName("page").description("시작페이지")
                                ,parameterWithName("size").description("한페이지 사이즈")
                                ,parameterWithName("sort").description("정렬조건")
                        )
                        ,links(
                                linkWithRel("self").description("API 자신에 대한 링크정보")
                                ,linkWithRel("document-create").description("문서 생성에 대한 링크정보")
                                ,linkWithRel("profile").description("RestDoc에 대한 링크정보")
                        )
                        ,requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("ACCEPT HEADER")
                        )
                        ,responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                                ,headerWithName(HttpHeaders.CACHE_CONTROL).description("CACHE_CONTROL")
                        )
                        ,relaxedResponseFields(
//                        ,responseFields(
                                fieldWithPath("_embedded.documentResponseList").description("문서목록")
                                ,fieldWithPath("_links.self").description("API 자신에 대한 링크정보")
                                ,fieldWithPath("_links.document-create").description("문서 생성에 대한 링크정보")
                                ,fieldWithPath("_links.profile").description("RestDoc에 대한 링크정보")
                        )
                ))
        ;
    }

    @Test
    @WithMockCustomUser(username = "username1", roles = "USER")
    @TestDescription("기존의 문서 하나 조회하기")
    public void getDocument() throws Exception {
        //Given
        //로그인한 사용자 id
        Integer userId = getLoginId();

        List<Integer> approvalIds = new ArrayList<>();
        IntStream.range(0,4).forEach(i -> {
            //계정 생성
            Account accountParam = this.generateAccount(i);
            approvalIds.add(accountParam.getId());
        });
        System.out.println("approvalIds = " + approvalIds);
        Document document = this.generateDocument(approvalIds,userId);

        //When & Then
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/documents/{id}",document.getId())
                .accept(MediaTypes.HAL_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("category").exists())
                .andExpect(jsonPath("contents").value("buythings"))
                .andExpect(jsonPath("docStatus").exists())
                .andExpect(jsonPath("approvals").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("document-get"
                        ,pathParameters(
                                parameterWithName("id").description("문서번호")
                        )
                        ,links(
                                linkWithRel("self").description("API 자신에 대한 링크정보")
                                ,linkWithRel("approval").description("해당문서에 대한 결재 링크정보")
                                ,linkWithRel("profile").description("RestDoc에 대한 링크정보")
                        )
                        ,requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("ACCEPT HEADER")
                        )
                        ,responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                                ,headerWithName(HttpHeaders.CACHE_CONTROL).description("CACHE_CONTROL")
                        )
                        ,relaxedResponseFields(
//                        ,responseFields(
                                fieldWithPath("id").description("문서 번호")
                                , fieldWithPath("title").description("문서 제목")
                                ,fieldWithPath("category").description("문서 분류(EXPENSE:휴가,VACATION:지출)")
                                ,fieldWithPath("contents").description("문서 내용")
                                ,fieldWithPath("docStatus").description("문서 상태 " +
                                                                                    "[ARCHIVE: 내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서" +
                                                                                    ",OUTBOX: 내가 생성한 문서 중 결재 진행 중인 문서" +
                                                                                    ",INBOX: 내가 결재를 해야 할 문서]")
                                ,fieldWithPath("approvals").description("결재정보")
                                ,fieldWithPath("_links.self").description("API 자신에 대한 링크정보")
                                ,fieldWithPath("_links.approval").description("결재한 문서에 대한 링크정보")
                                ,fieldWithPath("_links.profile").description("RestDoc에 대한 링크정보")
                        )
                ))
        ;

    }

    @Test
    @WithMockCustomUser(username = "username1", roles = "USER")
    @TestDescription("정상적으로 도큐먼트와 도큐먼트의 결재를 생성하는 테스트")
    public void createDocument() throws Exception {

        //로그인한 사용자 id
        Integer userId = getLoginId();

        List<Integer> approvalIds = new ArrayList<>();
        IntStream.range(0,4).forEach(i -> {
            //계정 생성
            Account accountParam = this.generateAccount(i);
            approvalIds.add(accountParam.getId());
        });
        //문서 생성, 결재권자id, 로그인한 사용자id를 파라미터 인자로 넘기기
        //Document를 시리얼라이징해서 인자로 넘길경우 양방향 참조때문에 스택오버플로우 발생
        //리퀘스트dto사용
        Document document = Document.createDocument("docutitle", Document.Category.EXPENSE,"buythings",approvalIds,userId);
        DocumentRequest documentRequest = DocumentRequest.from(document);

        mockMvc.perform(post("/api/documents/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(documentRequest))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("category").exists())
                .andExpect(jsonPath("contents").exists())
                .andExpect(jsonPath("approvals").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.approval").exists())
                .andExpect(jsonPath("_links.document-list").exists())
                .andExpect(jsonPath("_links.document-get").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("document-create"
                        ,links(
                                linkWithRel("self").description("API 자신에 대한 링크정보")
                                ,linkWithRel("approval").description("해당문서에 대한 결재 링크정보")
                                ,linkWithRel("document-list").description("문서목록에 대한 링크정보")
                                ,linkWithRel("document-get").description("결재한 문서에 대한 링크정보")
                                ,linkWithRel("profile").description("RestDoc에 대한 링크정보")
                        )
                        ,requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("ACCEPT HEADER")
                                ,headerWithName(HttpHeaders.CONTENT_LENGTH).description("CONTENT LENGTH")
                        )
//                        ,relaxedRequestFields(
                        ,requestFields(
                                fieldWithPath("title").description("문서 제목")
                                ,fieldWithPath("category").description("문서 분류(EXPENSE:휴가,VACATION:지출)")
                                ,fieldWithPath("contents").description("문서 내용")
                                ,fieldWithPath("docStatus").description("문서 상태 " +
                                        "[ARCHIVE: 내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서" +
                                        ",OUTBOX: 내가 생성한 문서 중 결재 진행 중인 문서" +
                                        ",INBOX: 내가 결재를 해야 할 문서]")
                                ,fieldWithPath("approvalIds").description("결재자 ID")
                        )
                        ,responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header")
                                ,headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                                ,headerWithName(HttpHeaders.CACHE_CONTROL).description("CACHE_CONTROL")
                                ,headerWithName(HttpHeaders.PRAGMA).description("PRAGMA")
                                ,headerWithName(HttpHeaders.EXPIRES).description("EXPIRES")
                        )
                        ,relaxedResponseFields(
                                fieldWithPath("id").description("문서 번호")
                                , fieldWithPath("title").description("문서 제목")
                                ,fieldWithPath("category").description("문서 분류(EXPENSE:휴가,VACATION:지출)")
                                ,fieldWithPath("contents").description("문서 내용")
                                ,fieldWithPath("docStatus").description("문서 상태 " +
                                        "[ARCHIVE: 내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서" +
                                        ",OUTBOX: 내가 생성한 문서 중 결재 진행 중인 문서" +
                                        ",INBOX: 내가 결재를 해야 할 문서]")
                                ,fieldWithPath("approvals").description("결재정보")
                                ,fieldWithPath("_links.self").description("API 자신에 대한 링크정보")
                                ,fieldWithPath("_links.approval").description("결재한 문서에 대한 링크정보")
                                ,fieldWithPath("_links.profile").description("RestDoc에 대한 링크정보")
                        )
                ))
                ;
    }

//    @Test
//    @WithMockCustomUser(username = "username1", roles = "USER")
//    @TestDescription("정상적으로 도큐먼트를 수정하는 테스트")
//    public void updateDocument() throws Exception {
//        //Given
//        //로그인한 사용자 id
//        Integer userId = getLoginId();
//
//        List<Integer> approvalIds = new ArrayList<>();
//        approvalIds.add(userId);
//        IntStream.range(0,4).forEach(i -> {
//            //계정 생성
//            Account accountParam = this.generateAccount(i);
//            approvalIds.add(accountParam.getId());
//        });
//
//        //문서 생성, 결재권자id, 로그인한 사용자id를 파라미터 인자로 넘기기
//        Document document = this.generateDocument(approvalIds,userId);
//        Document updateDoc = Document.createDocument("docutitle-update", Document.Category.EXPENSE,"buythings",approvalIds,userId);
//        document.update(updateDoc);
//        DocumentRequest documentRequest = DocumentRequest.from(document);
//
//        //When & Then
//        this.mockMvc.perform(put("/api/documents/{id}"
//                , document.getId()
//        )
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaTypes.HAL_JSON)
//                .content(this.objectMapper.writeValueAsBytes(documentRequest))
//        )
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("id").exists())
//                .andExpect(jsonPath("title").exists())
//                .andExpect(jsonPath("title").value("docutitle-update"))
////                .andExpect(jsonPath("_links.self").exists())
////                .andExpect(jsonPath("_links.profile").exists())
////                .andDo(document("get-an-event"))
//        ;
//    }

    @Test
    @WithMockCustomUser(username = "username1", roles = "USER")
    @TestDescription("정상적으로 도큐먼트를 삭제하는 테스트")
    public void deleteDocument() throws Exception {
        //Given
        //로그인한 사용자 id
        Integer userId = getLoginId();

        List<Integer> approvalIds = new ArrayList<>();
        approvalIds.add(userId);
        IntStream.range(0,4).forEach(i -> {
            //계정 생성
            Account accountParam = this.generateAccount(i);
            approvalIds.add(accountParam.getId());
        });

        Document document = this.generateDocument(approvalIds,userId);

        //When & Then
        this.mockMvc.perform(delete("/api/documents/{id}"
                , document.getId()
                ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").doesNotExist())
//                .andExpect(jsonPath("_links.self").exists())
//                .andExpect(jsonPath("_links.profile").exists())
//                .andDo(document("get-an-event"))
        ;
    }

    private Integer getLoginId(){
        // 로그인한 사용자의 id를 userId로 설정
        // email은 writerEmail로 설정
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserAccount userAccount = (UserAccount) principal;
        Account userDetails = userAccount.getAccount();
        Integer userId = userDetails.getId();
        return userId;
    }

    private Document generateDocument(List<Integer> approvalIds ,Integer userId) {
        Document document = Document.createDocument("docutitle", Document.Category.EXPENSE,"buythings",approvalIds,userId);
        return this.documentRepository.save(document);
    }

    private Account generateAccount(int index){
        Account account = new Account("gimun" + index + "@naver.com","1234");
        return this.accountRepository.save(account);
    }

}