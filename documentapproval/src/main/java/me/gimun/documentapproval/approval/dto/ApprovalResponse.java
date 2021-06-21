package me.gimun.documentapproval.approval.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.approval.Approval;

@Getter
@RequiredArgsConstructor
public class ApprovalResponse {
    private final Long id;
    //양방향 관계라 스택오버플로우 발생하기때문에 결재에서는 그냥 doc id만 응답으로 내보내줌
    private final Long documentId;
    private final Integer userId;
    private final Integer approveOrder;
    private final Approval.ApprovalStatus approvalStatus;
    private final String opinion;

    public static ApprovalResponse from(final Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getDocument().getId(),
                approval.getUserId(),
                approval.getApproveOrder(),
                approval.getApprovalStatus(),
                approval.getOpinion()
        );
    }

}
