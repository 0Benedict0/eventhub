package com.vlad.eventhub.controller;

import com.vlad.eventhub.dto.response.CategoryResponse;
import com.vlad.eventhub.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        var categories = categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName())).toList();
        return ResponseEntity.ok(categories);
    }
}
