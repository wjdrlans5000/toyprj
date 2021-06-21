package me.gimun.documentapproval.approval;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ApprovalRepository extends JpaRepository<Approval,Long> {

}
