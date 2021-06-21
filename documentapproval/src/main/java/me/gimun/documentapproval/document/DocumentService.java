package me.gimun.documentapproval.document;

import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.document.dto.DocumentRequest;
import me.gimun.documentapproval.document.dto.DocumentResponse;
import me.gimun.documentapproval.document.errors.DocumentNotFoundException;
import me.gimun.documentapproval.document.validator.DocumentValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository repository;
    private final DocumentValidator validator;

//    @PersistenceContext    // EntityManagerFactory가 DI 할 수 있도록 어노테이션 설정
//    private EntityManager em;

    public Page<Document> getDocuments(final Pageable pageable, final Integer accountId) {
//        List<Document> list =  read(em, accountId);
//        List<Document> list = repository.findAllByApproval(accountId);
        // 조인해서 accountId(로그인id)가 approval의 userId와 같거나 document List<Approval>에 approvalIds 에 있는경우의 document
        return repository.findAllByApproval(accountId,pageable);
    }

    public DocumentResponse getDocument(final long id) {
        final Document document = findById(id);
        return DocumentResponse.from(document);
    }

    @Transactional
    public long createDocument(final DocumentRequest request, final Integer accountId) {
        final Document document = request.toEntity2(accountId);
        validator.validate(document);
        return repository.save(document).getId();
    }

    @Transactional
    public void updateDocument(final long id, final DocumentRequest request, final Integer accountId) {
        final Document document = findById(id);
        document.update(request.toEntity2(accountId));
        validator.validate(document);
    }


    @Transactional
    public void deleteDocument(final long id) {
        final Document document = findById(id);
        repository.delete(document);
    }

    private Document findById(final long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id + "is not found"));
    }

//    private List<Document> read(EntityManager em, final Integer accountId) {
//        String jpql2 = "select d from Approval a join Document d on a.document = d where a.userId = :userId";
////        String jpql = "select d from Document d join fetch d.approvals where " + "a.user_id=:userId";
//        List<Document> resultList = em.createQuery(jpql2, Document.class)
//                .setParameter("userId", accountId).getResultList();
//        return resultList;
//    }
}