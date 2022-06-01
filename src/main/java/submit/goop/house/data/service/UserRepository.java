package submit.goop.house.data.service;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import submit.goop.house.data.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(String username);
}