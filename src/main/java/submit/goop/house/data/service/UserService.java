package submit.goop.house.data.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import submit.goop.house.data.entity.GoopUser;
import submit.goop.house.data.entity.User;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService {

    private UserRepository repository;

    public UserService(@Autowired UserRepository repository) {
        this.repository = repository;
    }


    public Optional<User> get(UUID id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public User findByUsername(String username){ return repository.findByUsername(username);}

    public int count() {
        return (int) repository.count();
    }

}
