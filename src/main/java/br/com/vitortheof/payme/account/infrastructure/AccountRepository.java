package br.com.vitortheof.payme.account.infrastructure;

import br.com.vitortheof.payme.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByCustomerId(UUID customerId);

}
