package br.com.vitortheof.payme.category.application.mapper;

import br.com.vitortheof.payme.category.application.dto.CategoryResponseDTO;
import br.com.vitortheof.payme.category.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponse(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getCustomerId(),
                category.getName(),
                category.getType()
        );
    }
}
