package me.gimun.documentapproval.document;


import org.springframework.data.jpa.repository.JpaRepository;


public interface DocumentRepository extends JpaRepository<Document,Long>, DocumentRepositoryCustom {

}
