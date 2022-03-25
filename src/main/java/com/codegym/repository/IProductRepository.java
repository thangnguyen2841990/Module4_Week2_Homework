package com.codegym.repository;

import com.codegym.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends PagingAndSortingRepository<Product, Long> {

    Page<Product> findByNameContaining(String name, Pageable pageable);

    @Query(value = "call findByCategory1(?1)" ,nativeQuery = true)
    Page<Product> findByCategory(Long categoryId);

}
