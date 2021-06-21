package me.gimun.documentapproval.document.validator;

import lombok.RequiredArgsConstructor;
import me.gimun.documentapproval.accounts.Account;
import me.gimun.documentapproval.accounts.AccountRepository;
import me.gimun.documentapproval.approval.Approval;
import me.gimun.documentapproval.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentValidator {
    private final AccountRepository accountRepository;

    public void validate(final Document document) {
        final BeanPropertyBindingResult result = new BeanPropertyBindingResult(document, Document.class.getSimpleName());

        final List<Approval> approvals = document.getApprovals();
        final List<Integer> approvalIds = approvals.stream()
                .map(Approval::getUserId)
                .collect(Collectors.toList());
        final List<Account> accounts = accountRepository.findAllById(approvalIds);
        //결재자 id 리스트와  account 사이즈가 다를경우 유효하지않은 id가 존재함.
        if (approvalIds.size() != accounts.size()) {
            // 유효하지 않은 승인자가 존재함
            // 예외
            result.rejectValue("approvals", "approvals.not_valid", "유효하지 않는 승인자가 있습니다.");
        }

        if (result.hasErrors()) {
            throw new RuntimeException();
        }
    }
}
