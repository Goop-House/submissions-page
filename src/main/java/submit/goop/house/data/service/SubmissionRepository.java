package submit.goop.house.data.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findBySubmissionID(UUID submissionID);

}