package me.gimun.documentapproval.approval;

import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.approval.dto.ApprovalResponse;
import me.gimun.documentapproval.approval.errors.ApprovalNotFoundException;
import me.gimun.documentapproval.auth.support.AuthUser;
import me.gimun.documentapproval.common.ErrorsResource;
import me.gimun.documentapproval.document.DocumentController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/api/documents/{documentId}/approval/{approvalStatus}")
    public ResponseEntity approval(
            @PathVariable Long documentId,
            @PathVariable Approval.ApprovalStatus approvalStatus,
            @AuthUser final Account account,
            @RequestBody Map<String,Object> opinion,
            final BindingResult result
    ) {
//      업데이트할 approvalStatus > WAITTING 일경우 badRequest
        if(approvalStatus == Approval.ApprovalStatus.WAITING){
            return badRequest(result);
        }

        final Approval approval = approvalService.approval(documentId, approvalStatus, account.getId(), opinion.get("opinion").toString());
        final ApprovalResponse approvalResponse = approvalService.getApproval(approval.getId());

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(ApprovalController.class).approval(documentId,approvalStatus,account,opinion,result));
        URI createUri = selfLinkBuilder.toUri();
        ApprovalResource approvalResource = new ApprovalResource(approvalResponse,documentId,approvalStatus,account,opinion,result);

        approvalResource.add(linkTo(methodOn(DocumentController.class).getDocument(documentId)).withRel("document-get"));

        approvalResource.add(new Link("/docs/index.html#resources-approval").withRel("profile"));

        return ResponseEntity.created(createUri).body(approvalResource);
    }


    /*
     * ApprovalNotFoundException 발생시 스프링에서 자동으로 맵핑
     * */
    @ExceptionHandler(ApprovalNotFoundException.class)
    public ResponseEntity<Void> handleDocumentNotFound(ApprovalNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    // badRequst 발생시 index로 가는 리소스 제공
    private ResponseEntity badRequest(BindingResult errors){
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
