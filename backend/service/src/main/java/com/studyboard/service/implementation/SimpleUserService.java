package com.studyboard.service.implementation;

import com.studyboard.exception.UniqueConstraintException;
import com.studyboard.exception.UserDoesNotExist;
import com.studyboard.model.Authorities;
import com.studyboard.model.User;
import com.studyboard.repository.AuthoritiesRepository;
import com.studyboard.repository.UserRepository;
import com.studyboard.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Service used to manage users. Performs user creation, getting, and update of user password */
@Service
public class SimpleUserService implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUser(Long id) {
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new UserDoesNotExist();
        }
        logger.info("Getting user with id " + id);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findOneByUsername(username);
        if (user == null) {
            throw new UserDoesNotExist();
        }
        logger.info("Getting user with username " + username);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        logger.info("Getting all users.");
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user) throws UniqueConstraintException {
        try {
            authoritiesRepository.save(new Authorities(user.getUsername(), "USER"));
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setRole("USER");
            user.setEnabled(true);
            user.setPassword(hashedPassword);
            logger.info("Created new user with username " + user.getUsername());
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (((Collection<?>) authoritiesRepository.findAll()).size() != userRepository.findAll().size()) {
                Optional<Authorities> auth = authoritiesRepository.findOneByUsername(user.getUsername());
                authoritiesRepository.deleteById(auth.get().getId());
            }
            throw new UniqueConstraintException("User with the same username or email already exists.");
        }
    }

    @Override
    public User updateUserPassword(User user) {
        User storedUser = userRepository.findUserById(user.getId());
        if (storedUser == null) {
            throw new UserDoesNotExist();
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        storedUser.setPassword(hashedPassword);
        logger.info("Password of user with username" + user.getUsername() + " was updated");
        return userRepository.save(storedUser);
    }

    @Override
    public User resetLoginAttempts(Long id) {
        User user = userRepository.findUserById(id);
        if (user == null) {
            throw new UserDoesNotExist();
        }
        user.setLoginAttempts(0);
        logger.info("Login attempts of user with username " + user.getUsername() + " were reset");
        return userRepository.save(user);
    }
}