package br.com.vitortheof.payme.category.application;

import br.com.vitortheof.payme.category.application.dto.CategoryRequestDTO;
import br.com.vitortheof.payme.category.application.dto.CategoryResponseDTO;
import br.com.vitortheof.payme.category.application.mapper.CategoryMapper;
import br.com.vitortheof.payme.category.domain.Category;
import br.com.vitortheof.payme.category.infrastructure.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponseDTO create(CategoryRequestDTO request, UUID customerId) {
        Category category = Category.builder()
                .customerId(customerId)
                .name(request.name())
                .type(request.type())
                .build();
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public List<CategoryResponseDTO> findAllByCustomerId(UUID customerId) {
        return categoryRepository.findAllByCustomerId(customerId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
