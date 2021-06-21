package me.gimun.documentapproval.approval;

import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.approval.dto.ApprovalResponse;
import me.gimun.documentapproval.approval.errors.ApprovalNotFoundException;
import me.gimun.documentapproval.document.Document;
import me.gimun.documentapproval.document.DocumentRepository;
import me.gimun.documentapproval.document.errors.DocumentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalRepository repository;
    private final DocumentRepository documentRepository;

    public ApprovalResponse getApproval(final long id) {
        final Approval approval = findById(id);
        return ApprovalResponse.from(approval);
    }

    private Approval findById(final long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApprovalNotFoundException(id + "is not found"));
    }

    @Transactional
    public Approval approval(Long documentId, Approval.ApprovalStatus approvalStatus, Integer accountId, String opinion) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        // 문서의 결재정보들 가져와서 로그인한 id가 결재자 리스트에 있는지 확인
        List<Approval> approvals = document.getApprovals();
        boolean isApproval = approvals.stream().anyMatch(approval -> approval.getUserId().equals(accountId));
        // 없으면 throw -> 내가 결재자가 아닌경우
        if (!isApproval) {
            throw new IllegalStateException();
        }
        // 내가 결재를 해야 할 문서가 아닌경우
        if (document.getDocStatus() != Document.DocStatus.INBOX) {
            throw new IllegalStateException();
        }

        // 결재자 id 리스트 추출
        List<Integer> approvalIds = approvals.stream().map(Approval::getUserId).collect(Collectors.toList());
        // 로그인한 사용자의 index 추출
        int index = approvalIds.indexOf(accountId);
        // 첫번째 순서부터 내 순서까지 돌면서 approvalStatus가 ACCEPT가 아닌경우 (REJECT나 WAITING) THROW
        IntStream.range(0, index).forEach(idx -> {
            Approval approval = approvals.get(idx);
            if (!approval.isAvailableStatus()) {
                throw new IllegalStateException();
            }
        });

        Approval approval = approvals.get(index);

        //승인시 나한테는 문서상태 OUTBOX
//        document.changeDocument(Document.DocStatus.OUTBOX);

        // index가 approvalIds.size()랑 같은경우 > 마지막 결재자인 경우 document.DocStatus = ARCHIVE로 수정
        // approvalStatus가 REJECT인 경우 document.DocStatus = ARCHIVE로 수정
        if(index == approvalIds.size()-1 || (approvalStatus == Approval.ApprovalStatus.REJECT)){
            document.changeDocument(Document.DocStatus.ARCHIVE);
        }
        //문서수정
        approval.approval_document(document);
        //결재상태수정
        approval.approval(approvalStatus);
        //의견수정
        approval.approval_opinion(opinion);
        return approval;
    }
}
