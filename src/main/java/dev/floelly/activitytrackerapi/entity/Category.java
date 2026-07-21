package dev.floelly.activitytrackerapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @NotNull
    @Column(nullable = false, unique = true, updatable = false, length = 13)
    private String businessId;

    @NotNull
    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 9)
    private String colorCode;

    @Column(length = 30)
    private String iconName;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "category")
    private List<SubCategory> subCategories;
}
