package me.gimun.documentapproval.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

//Repository명 + Custom >> 명명규칙 해당이름으로 정의하면 jpa에서 매핑해줌
public interface DocumentRepositoryCustom {
    List<Document> findAllByApproval(Integer accountId);
    Page<Document> findAllByApproval(Integer accountId, Pageable pageable);
}
