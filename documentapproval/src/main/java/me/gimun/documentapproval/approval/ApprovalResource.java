package me.gimun.documentapproval.approval;

import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.approval.dto.ApprovalResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.BindingResult;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ApprovalResource extends EntityModel<ApprovalResponse> {
    public ApprovalResource(ApprovalResponse content,
                            Long documentId,
                            Approval.ApprovalStatus approvalStatus,
                            final Account account,
                            Map<String,Object> opinion,
                            BindingResult result,
                            Link... links) {
        super(content, links);
        // withSelfRel(): 리소스에 대한 링크를 type-safe 한 method로 제공한다.
        add(linkTo(methodOn(ApprovalController.class).approval(documentId,approvalStatus,account,opinion,result)).withSelfRel());
    }
}