package me.gimun.documentapproval.approval;

import lombok.*;
import me.gimun.documentapproval.document.Document;
import me.gimun.documentapproval.domain.BaseEntity;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "approval")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class Approval extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    private Document document;

    // 메일로설정할 경우 해당 계정 삭제후 재 생성시 기존 권한을 그대로 사용할 여지가 있어서 id로 작성자 설정
    @Column(nullable = false)
    private Integer userId;

    //
//    @Column(nullable = false)
//    private String writerEmail;
//
//    @Column(nullable = false)
//    private String approverEmail;
    //

    @Column(nullable = false)
    private int approveOrder;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @Column
    private String opinion;

    @Builder
    public Approval(Document document, Integer userId, Integer approveOrder, ApprovalStatus approvalStatus, String opinion) {
        this.document = document;
        this.userId = userId;
        this.approveOrder = approveOrder;
        this.approvalStatus = approvalStatus;
        this.opinion = opinion;
    }

    public void update(final Approval entity) {
        this.document = entity.getDocument();
        this.userId = entity.getUserId();
        this.approveOrder = entity.getApproveOrder();
        this.approvalStatus = entity.getApprovalStatus();
        this.opinion = entity.getOpinion();
    }

    public void changeDocument(Document document) {
        this.document = document;
    }

    public boolean isAvailableStatus() {
        return this.approvalStatus == ApprovalStatus.ACCEPT;
    }

    public void approval(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public void approval_document(Document document) {
        this.document = document;
    }

    public void approval_opinion(String opinion) {
        this.opinion = opinion;
    }

    // 결재상태
    public enum ApprovalStatus {
        // 승인
        ACCEPT,
        // 거절
        REJECT,
        // 대기(기본)
        WAITING,
        ;
    }
}


