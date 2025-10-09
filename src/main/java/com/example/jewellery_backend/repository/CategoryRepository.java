//In repository we are going to save those thongs
//we call JPA and extend it because we are going to use it

package com.example.jewellery_backend.repository;

import com.example.jewellery_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Here <Entity Name , Primary key Data type >
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

