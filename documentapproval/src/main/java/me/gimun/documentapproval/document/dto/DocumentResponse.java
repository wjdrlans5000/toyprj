package me.gimun.documentapproval.document.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.approval.dto.ApprovalResponse;
import me.gimun.documentapproval.document.Document;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class DocumentResponse {
    private final long id;
    private final String title;
    private final Document.Category category;
    private final String contents;
    private final Document.DocStatus docStatus;
    private final List<ApprovalResponse> approvals;

    public static DocumentResponse from(final Document document) {
        //엔터티의 경우 그대로 사용하게되면 안됨
        List<ApprovalResponse> list = document.getApprovals().stream()
                .map(approval -> ApprovalResponse.from(approval)).collect(Collectors.toList());
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getCategory(),
                document.getContents(),
                document.getDocStatus(),
                list
                );
    }

}
