package dev.kristof.spendly.user;

import dev.kristof.spendly.common.exception.EmailAlreadyInUseException;
import dev.kristof.spendly.common.exception.InvalidCredentialsException;
import dev.kristof.spendly.common.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid credentials"
                ));
    }

    public User createUser(String displayName, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Email already in use: " + email);
        }

        User user = new User(email, password, displayName, UserRole.USER);
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


}
