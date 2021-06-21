package me.gimun.documentapproval.document;

import lombok.*;
import me.gimun.documentapproval.approval.Approval;
import me.gimun.documentapproval.domain.BaseEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Entity
@Table(name = "document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class Document extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private String contents;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocStatus docStatus;
    /*
    * 맵핑되어 document 생성시 approvals 객체도 함께 생성
    * approval 와 양방향 참조
    * */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Approval> approvals = new ArrayList<>();

//    public Document(final String title, final Category category, final String contents, final DocStatus docStatus) {
//        this.title = title;
//        this.category = category;
//        this.contents = contents;
//        this.docStatus = docStatus;
//    }

    public Document(String title, Category category, String contents, DocStatus docStatus, List<Approval> approvals) {
        this.title = title;
        this.category = category;
        this.contents = contents;
        this.docStatus = docStatus;

        approvals.stream().forEach(approval -> approval.changeDocument(this));
        this.approvals = approvals;
    }

    public void update(final Document entity) {
        this.title = entity.getTitle();
        this.category = entity.getCategory();
        this.contents = entity.getContents();
        this.docStatus = entity.getDocStatus();
        // list 객체로
        // approvals = proxy
        this.approvals.clear();
        // approvals = proxy
        this.approvals.addAll(entity.getApprovals());
    }


    public static Document createDocument(String title, Category category, String contents, List<Integer> approvalIds, Integer accountId) {
        // 결재 승인자는 1명 이하가 될수 없음
        if (approvalIds.size() < 1) {
            throw new IllegalArgumentException();
        }

        //approvalIds.size() 만큼 Approval 객체 생성
        List<Approval> approvals = IntStream.range(0, approvalIds.size())
                .mapToObj(idx -> Approval.builder()
                        .userId(approvalIds.get(idx))
                        .approveOrder(idx)
                        .approvalStatus(Approval.ApprovalStatus.WAITING)
                        .build())
                .collect(Collectors.toList());

        Document.DocStatus docStatus;
        // 로그인한 사용자 id가 approvalIds에 있으면 INBOX
        if (approvalIds.contains(accountId)) {
            docStatus = Document.DocStatus.INBOX;
        } else {
            docStatus = Document.DocStatus.OUTBOX;
        }

        //Document 객체 리턴 (Approval 객체 리스트 포함)
        return new Document(
                title,
                category,
                contents,
                docStatus,
                approvals
        );
    }


    public void changeDocument(DocStatus docStatus) {
        this.docStatus = docStatus;
    }

    // 문서 분류
    public enum Category {
        // 지출
        EXPENSE,
        // 휴가
        VACATION,
        ;
    }

    // 문서 상태
    public enum DocStatus {
        // 내가 관여한 문서 중 결재가 완료(승인 또는 거절)된 문서
        ARCHIVE,
        // 내가 생성한 문서 중 결재 진행 중인 문서
        OUTBOX,
        // 내가 결재를 해야 할 문서
        INBOX
        ;
    }
}
