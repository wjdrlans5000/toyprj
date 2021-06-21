package me.gimun.documentapproval.document;

import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.approval.ApprovalController;
import me.gimun.documentapproval.auth.model.UserAccount;
import me.gimun.documentapproval.auth.support.AuthUser;
import me.gimun.documentapproval.common.ErrorsResource;
import me.gimun.documentapproval.document.dto.DocumentRequest;
import me.gimun.documentapproval.document.dto.DocumentResponse;
import me.gimun.documentapproval.document.errors.DocumentNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    private final PagedResourcesAssembler<DocumentResponse> assembler;

    @GetMapping
    public ResponseEntity getDocuments(Pageable pageable,
                                       PagedResourcesAssembler<DocumentResponse> assembler,
                                       @AuthUser final Account account
    ) {


        Page<Document> page = service.getDocuments(pageable, account.getId());
        //page 제네릭타입 DocumentResponse으로 변환
        Page<DocumentResponse> page2 = page.map(document -> DocumentResponse.from(document));
        //Page 를 페이징처리가 된 Model 목록으로 변환해준다.
        //e-> new EventResource(e) > 각 Event를 EventResource 로 변환 작업
        PagedModel pagedResources = assembler.toModel(page2, e -> new DocumentResource(e));
        //create 링크정보 추가
        pagedResources.add(linkTo(DocumentController.class).withRel("document-create"));
        //Profile 에 대한 링크 정보만 추가
        pagedResources.add(new Link("/docs/index.html#resources-document-list").withRel("profile"));
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getDocument(@PathVariable Long id) {

        DocumentResponse documentResponse = service.getDocument(id);
        DocumentResource documentResource = new DocumentResource(documentResponse);
        documentResource.add(linkTo(methodOn(ApprovalController.class).approval(id,null,null,null,null)).withRel("approval"));
        documentResource.add(new Link("/docs/index.html#resources-document-get").withRel("profile"));
        return ResponseEntity.ok(documentResource);
    }

    @PostMapping
    public ResponseEntity createDocument(
            @RequestBody final DocumentRequest request,
            final BindingResult result,
            @AuthUser final Account account) {

        if(result.hasErrors()){
            return badRequest(result);
        }

        final long documentId = service.createDocument(request, account.getId());
        final DocumentResponse document = service.getDocument(documentId);

        /*
         * Location URI 만들기
         * HATEOS가 제공하는 linkTo(), methodOn() 등 사용하여 uri 생성
         * */
        WebMvcLinkBuilder selfLinkBuilder = linkTo(DocumentController.class).slash(document.getId());
        URI createUri = selfLinkBuilder.toUri();
        DocumentResource documentResource = new DocumentResource(document);
//        withRel(): 이 링크가 리소스와 어떤 관계에 있는지 관계를 정의할 수 있다.
//        Relation과 HREF 만 제공.
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("id,DESC"));

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserAccount userAccount = (UserAccount) principal;
        Account userDetails = userAccount.getAccount();

        documentResource.add(linkTo(methodOn(ApprovalController.class).approval(documentId,null,null,null,null)).withRel("approval"));
        documentResource.add(linkTo(methodOn(DocumentController.class).getDocuments(pageRequest, assembler, userDetails)).withRel("document-list"));
        documentResource.add(linkTo(methodOn(DocumentController.class).getDocument(document.getId())).withRel("document-get"));
//        //profile Link 추가
        documentResource.add(new Link("/docs/index.html#resources-document-create").withRel("profile"));
        return ResponseEntity.created(createUri).body(documentResource);
    }

    @PutMapping("{id}")
    public ResponseEntity<DocumentResponse> updateDocument(@PathVariable final Long id,
                                                           @RequestBody final DocumentRequest request,
                                                           final BindingResult result,
                                                           @AuthUser final Account account) {
        if(result.hasErrors()){
            return badRequest(result);
        }

        service.updateDocument(id, request, account.getId());

        final DocumentResponse document = service.getDocument(id);
        return ResponseEntity.ok(document);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable final Long id) {
        service.deleteDocument(id);
        return ResponseEntity.ok().build();
    }

    /*
     * DocumentNotFoundException 발생시 스프링에서 자동으로 맵핑
     * */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Void> handleDocumentNotFound(DocumentNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    // badRequst 발생시 index로 가는 리소스 제공
    private ResponseEntity badRequest(BindingResult errors){
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

}
