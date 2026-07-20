package com.fattiebakery.service;

import com.fattiebakery.model.Category;
import com.fattiebakery.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Dành cho User (Chỉ hiển thị danh mục đang hoạt động)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    // 2. Dành cho Admin (Hiển thị tất cả để quản lý)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 3. Các hàm CRUD cơ bản
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}