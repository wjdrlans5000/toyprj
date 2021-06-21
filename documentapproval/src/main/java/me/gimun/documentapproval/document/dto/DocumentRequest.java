package me.gimun.documentapproval.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.gimun.documentapproval.document.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class DocumentRequest {
    @NotEmpty
    private String title;

    @NotNull
    private Document.Category category;

    @NotEmpty
    private String contents;

    @NotEmpty
    private Document.DocStatus docStatus;

    @NotEmpty
    private List<Integer> approvalIds = new ArrayList<>();

//    public Document toEntity() {
//        return new Document(this.title, this.category, this.contents, this.docStatus);
//    }

    public Document toEntity2(final Integer accountId) {
        return Document.createDocument(title, category, contents, approvalIds, accountId);
    }

    public static DocumentRequest from(final Document document) {
        return new DocumentRequest(
                document.getTitle(),
                document.getCategory(),
                document.getContents(),
                document.getDocStatus(),
                document.getApprovals().stream().map(approval -> approval.getUserId()).collect(Collectors.toList())

        );
    }
}
