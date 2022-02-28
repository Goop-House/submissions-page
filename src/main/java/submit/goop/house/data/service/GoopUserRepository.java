package submit.goop.house.data.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import submit.goop.house.data.entity.GoopUser;

public interface GoopUserRepository extends JpaRepository<GoopUser, UUID> {

    List<GoopUser> findByDiscordID(String discordID);

}