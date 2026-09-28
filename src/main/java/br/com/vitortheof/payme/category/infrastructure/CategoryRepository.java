package br.com.vitortheof.payme.category.infrastructure;

import br.com.vitortheof.payme.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByCustomerId(UUID customerId);

    Optional<Category> findByIdAndCustomerId(UUID id, UUID customerId);
}
