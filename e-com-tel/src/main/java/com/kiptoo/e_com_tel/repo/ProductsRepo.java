package com.kiptoo.e_com_tel.repo;

import com.kiptoo.e_com_tel.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepo extends JpaRepository<Products, Long> {
}
