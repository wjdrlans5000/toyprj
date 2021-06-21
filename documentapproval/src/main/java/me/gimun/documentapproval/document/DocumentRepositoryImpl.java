package me.gimun.documentapproval.document;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

import static me.gimun.documentapproval.approval.QApproval.approval;

//Repository명 + Impl >> 명명규칙 해당이름으로 정의하면 jpa에서 매핑해줌
public class DocumentRepositoryImpl implements DocumentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public DocumentRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Document> findAllByApproval(Integer accountId) {
        return queryFactory.select(approval.document)
                .from(approval)
                .join(approval.document)
                .where(approval.userId.eq(accountId))
                .fetch();

    }

    @Override
    public Page<Document>  findAllByApproval(Integer accountId, Pageable pageable) {
        QueryResults<Document> result = (QueryResults<Document>) queryFactory.select(approval.document)
                .from(approval)
                .join(approval.document)
                .where(approval.userId.eq(accountId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(result.getResults(),pageable,result.getTotal());

    }
}

