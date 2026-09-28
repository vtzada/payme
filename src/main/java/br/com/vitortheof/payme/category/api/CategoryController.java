package br.com.vitortheof.payme.category.api;

import br.com.vitortheof.payme.category.application.CategoryService;
import br.com.vitortheof.payme.category.application.dto.CategoryRequestDTO;
import br.com.vitortheof.payme.category.application.dto.CategoryResponseDTO;
import br.com.vitortheof.payme.user.domain.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO request, @AuthenticationPrincipal Customer customerLogado){
        CategoryResponseDTO response = categoryService.create(request, customerLogado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listByCustomer(@AuthenticationPrincipal Customer customerLogado){
        List<CategoryResponseDTO> response = categoryService.findAllByCustomerId(customerLogado.getId());
        return ResponseEntity.ok(response);
    }
}
