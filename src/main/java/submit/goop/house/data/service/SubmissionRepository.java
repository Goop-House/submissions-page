package submit.goop.house.data.service;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import submit.goop.house.data.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

}