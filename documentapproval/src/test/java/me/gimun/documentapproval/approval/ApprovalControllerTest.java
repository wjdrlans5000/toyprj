package me.gimun.documentapproval.approval;

import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.accounts.AccountRepository;
import me.gimun.documentapproval.auth.model.UserAccount;
import me.gimun.documentapproval.common.BaseControllerTest;
import me.gimun.documentapproval.common.TestDescription;
import me.gimun.documentapproval.config.WithMockCustomUser;
import me.gimun.documentapproval.document.Document;
import me.gimun.documentapproval.document.DocumentRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class ApprovalControllerTest extends BaseControllerTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ApprovalRepository approvalRepository;

    @Autowired
    DocumentRepository documentRepository;


    @Test
    @WithMockCustomUser(username = "username1", roles = "USER")
    @TestDescription("??????????????? ????????? ??????(??????)?????? ?????????")
    public void approval() throws Exception {
        // ????????? ?????? ??? ?????? ??????
        // ???????????? ????????? id
        Account account = getLoginAccount();

        List<Integer> approvalIds = new ArrayList<>();
        approvalIds.add(account.getId());
        IntStream.range(0,4).forEach(i -> {
            //?????? ??????
            Account accountParam = this.generateAccount(i);
            approvalIds.add(accountParam.getId());
        });
        Document document = this.generateDocument(approvalIds,account.getId());
        Map<String, Object> opinion = new HashMap<>();
        opinion.put("opinion","opinion write");

        //When & Then
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/api/documents/{documentId}/approval/{approvalStatus}"
                , document.getId()
                , Approval.ApprovalStatus.ACCEPT
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(opinion))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("documentId").exists())
                .andExpect(jsonPath("userId").exists())
                .andExpect(jsonPath("approveOrder").exists())
                .andExpect(jsonPath("approvalStatus").value("ACCEPT"))
                .andExpect(jsonPath("opinion").value("opinion write"))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.document-get").exists())
                .andExpect(jsonPath("_links.profile").exists())
                //REST Docs ??????
                .andDo(document("approval"
                        ,pathParameters(
                            parameterWithName("documentId").description("????????????")
                            ,parameterWithName("approvalStatus").description("????????????")
                        )
                        ,links(
                                linkWithRel("self").description("API ????????? ?????? ????????????")
                                ,linkWithRel("document-get").description("????????? ????????? ?????? ????????????")
                                ,linkWithRel("profile").description("RestDoc??? ?????? ????????????")
                        )
                        ,requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("ACCEPT HEADER")
                                ,headerWithName(HttpHeaders.CONTENT_LENGTH).description("CONTENT LENGTH")
                        )
//                        ,relaxedRequestFields(
                        ,requestFields(
                                 fieldWithPath("opinion").description("?????? ??????")
                        )
                        ,responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header")
                                ,headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        )
                        ,relaxedResponseFields(
//                        ,responseFields(
                                fieldWithPath("id").description("?????? ??????")
                                , fieldWithPath("documentId").description("????????????")
                                ,fieldWithPath("userId").description("????????? ?????????")
                                ,fieldWithPath("approveOrder").description("????????????")
                                ,fieldWithPath("approvalStatus").description("????????????")
                                ,fieldWithPath("opinion").description("????????????")
                                ,fieldWithPath("_links.self").description("API ????????? ?????? ????????????")
                                ,fieldWithPath("_links.document-get").description("????????? ????????? ?????? ????????????")
                                ,fieldWithPath("_links.profile").description("RestDoc??? ?????? ????????????")
                        )
                ))
        ;
        // ?????? ?????? ?????????????????? ??????
//        this.mockMvc.perform(get("/api/documents/{id}",document.getId()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("id").exists())
//                .andExpect(jsonPath("title").exists())
//                .andExpect(jsonPath("docStatus").value("OUTBOX"))
////                .andExpect(jsonPath("_links.profile").exists())
////                .andDo(document("get-an-event"))
//        ;
    }

    private Account getLoginAccount(){
        // ???????????? ???????????? id??? userId??? ??????
        // email??? writerEmail??? ??????
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserAccount userAccount = (UserAccount) principal;
        Account userDetails = userAccount.getAccount();
        return userDetails;
    }

    private Account generateAccount(int index){
        Account account = new Account("gimun" + index + "@naver.com","1234");
        return this.accountRepository.save(account);
    }

    private Document generateDocument(List<Integer> approvalIds ,Integer userId) {
        Document document = Document.createDocument("docutitle", Document.Category.EXPENSE,"buythings",approvalIds,userId);
        return this.documentRepository.save(document);
    }

}
