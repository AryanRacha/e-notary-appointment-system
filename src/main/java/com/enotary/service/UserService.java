package com.enotary.service;

import com.enotary.model.Notary;
import com.enotary.model.Role;
import com.enotary.model.User;
import com.enotary.repository.NotaryRepository;
import com.enotary.repository.UserRepository;
import com.enotary.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NotaryRepository notaryRepository;

    public UserService(UserRepository userRepository, NotaryRepository notaryRepository) {
        this.userRepository = userRepository;
        this.notaryRepository = notaryRepository;
    }

    @Transactional
    public User registerClient(String fullName, String email, String rawPassword, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists");
        }
        String passwordHash = PasswordUtil.hashPassword(rawPassword);
        User user = new User(fullName, email, passwordHash, phone, Role.CLIENT);
        return userRepository.save(user);
    }

    @Transactional
    public Notary registerNotary(String fullName, String email, String rawPassword, String phone,
                                 String licenseNumber, String officeAddress, String specialization) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists");
        }
        if (notaryRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("A notary with license number " + licenseNumber + " already exists");
        }
        String passwordHash = PasswordUtil.hashPassword(rawPassword);
        User user = new User(fullName, email, passwordHash, phone, Role.NOTARY);
        User savedUser = userRepository.save(user);

        Notary notary = new Notary(savedUser, licenseNumber, officeAddress, specialization);
        return notaryRepository.save(notary);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<Notary> getAllNotaries() {
        return notaryRepository.findAll();
    }

    public Optional<Notary> getNotaryByUserId(Long userId) {
        return notaryRepository.findByUserId(userId);
    }
}
