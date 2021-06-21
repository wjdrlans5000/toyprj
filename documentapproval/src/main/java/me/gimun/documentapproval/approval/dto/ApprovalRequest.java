package me.gimun.documentapproval.approval.dto;

import lombok.Getter;
import me.gimun.documentapproval.approval.Approval;
import me.gimun.documentapproval.document.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
public class ApprovalRequest {
    @NotEmpty
    private Document document;

    @NotEmpty
    private Integer userId;


    @NotEmpty
    private Integer approveOrder;

    @NotNull
    private Approval.ApprovalStatus approvalStatus;

    private String opinion;

    public Approval toEntity() {
        return new Approval(this.document, this.userId, this.approveOrder, this.approvalStatus, this.opinion);
    }
}
