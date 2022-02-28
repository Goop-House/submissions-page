package submit.goop.house.data.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import submit.goop.house.data.entity.GoopUser;

@Service
public class GoopUserService {

    private GoopUserRepository repository;

    public GoopUserService(@Autowired GoopUserRepository repository) {
        this.repository = repository;
    }

    public Optional<GoopUser> get(UUID id) {
        return repository.findById(id);
    }

    public List<GoopUser> getAll() {
        return repository.findAll();
    }

    public List<GoopUser> findByDiscordID(String discordID){
        return repository.findByDiscordID(discordID);
    }

    public Page<GoopUser> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public GoopUser create(GoopUser goopUser) {
        return repository.save(goopUser);
    }


    public GoopUser update(GoopUser entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<GoopUser> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
