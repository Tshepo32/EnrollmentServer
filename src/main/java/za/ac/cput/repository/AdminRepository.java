package za.ac.cput.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.cput.Domain.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    // You can add custom query methods here if needed
}
