package softuniBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softuniBlog.entity.Category;

/**
 * Created by user on 12/11/2016.
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
