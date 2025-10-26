package gemshop.repo;

import gemshop.domain.Gemstone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GemstoneRepository extends JpaRepository<Gemstone, Long> {
    // we can add custom finders later if needed
}
