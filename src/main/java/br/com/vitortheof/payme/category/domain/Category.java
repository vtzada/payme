package br.com.vitortheof.payme.category.domain;

import br.com.vitortheof.payme.category.domain.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    private String name;

    private CategoryType type;

    private LocalDateTime createdAt;
}
