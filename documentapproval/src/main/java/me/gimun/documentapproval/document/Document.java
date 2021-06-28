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
//기본 생성자 접근을 protected으로 변경, 외부에서 해당 생성자를 접근 할 수 없으므로 아래 생성자를 통해서 객체를 생성 해야 한다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class Document extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    //Enum 상수값을 그대로 DB에 스트링으로 저장
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
    // FetchType.LAZY : 지연로딩 사용
    // 로딩되는 시점에 Lazy 로딩 설정이 되어있는 Approval 엔티티는 프록시 객체로 가져온다.
    // 후에 실제 객체를 사용하는 시점에(Approval을 사용하는 시점에) 초기화가 된다. DB에 쿼리가 나간다.
    // cascade = CascadeType.ALL : 모든 Cascade 적용. Entity의 상태 변화를 전파시키는 옵션이다.
    // Entity의 상태 변화가 있으면 연관되어 있는(ex. @OneToMany, @ManyToOne) Entity에도 상태 변화를 전이시키는 옵션이다.
    // orphanRemoval = true : Document 엔티티가 삭제될 때 참조가 끊어진 연관된 Approval 엔티티도 삭제하라는 의미
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
        // approvals 객체를 지연로딩으로 가져오므로 실제 객체를 가져오는게 아닌 프록시 객체를 가지고옴
        // 따라서 get을 사용하여 실제 사용해야 프록시 객체를 사용하여 실제 엔티티를 상속받은 객체를 반환함
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
