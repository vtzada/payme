package br.com.vitortheof.payme.category.service;

import br.com.vitortheof.payme.category.application.CategoryService;
import br.com.vitortheof.payme.category.application.dto.CategoryRequestDTO;
import br.com.vitortheof.payme.category.application.dto.CategoryResponseDTO;
import br.com.vitortheof.payme.category.application.mapper.CategoryMapper;
import br.com.vitortheof.payme.category.domain.Category;
import br.com.vitortheof.payme.category.infrastructure.CategoryRepository;
import br.com.vitortheof.payme.transaction.domain.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("Deve criar categoria vinculada ao customerId do token")
    void deveCriarCategoriaComCustomerDoToken() {
        UUID customerId = UUID.randomUUID();
        var request = new CategoryRequestDTO("Alimentação", TransactionType.DESPESA);
        var saved = Category.builder().id(UUID.randomUUID()).customerId(customerId).name("Alimentação").type(TransactionType.DESPESA).build();
        var response = new CategoryResponseDTO(saved.getId(), customerId, "Alimentação", TransactionType.DESPESA);

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response);

        CategoryResponseDTO result = categoryService.create(request, customerId);

        assertNotNull(result);
        assertEquals(customerId, result.customerId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve listar apenas categorias do customer informado")
    void deveListarCategoriasDoCustomer() {
        UUID customerId = UUID.randomUUID();
        var category = Category.builder().id(UUID.randomUUID()).customerId(customerId).name("Salário").type(TransactionType.RECEITA).build();
        var response = new CategoryResponseDTO(category.getId(), customerId, "Salário", TransactionType.RECEITA);

        when(categoryRepository.findAllByCustomerId(customerId)).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        var result = categoryService.findAllByCustomerId(customerId);

        assertEquals(1, result.size());
        assertEquals("Salário", result.get(0).name());
        verify(categoryRepository, times(1)).findAllByCustomerId(customerId);
    }
}
