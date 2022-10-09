package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}