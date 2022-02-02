package submit.goop.house.data.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import submit.goop.house.data.entity.Submission;

@Service
public class SubmissionService {

    private SubmissionRepository repository;

    public SubmissionService(@Autowired SubmissionRepository repository) {
        this.repository = repository;
    }

    public Optional<Submission> get(UUID id) {
        return repository.findById(id);
    }

    public Submission update(Submission entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Submission> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
