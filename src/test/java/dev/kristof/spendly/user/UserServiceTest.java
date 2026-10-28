package dev.kristof.spendly.user;

import dev.kristof.spendly.common.exception.EmailAlreadyInUseException;
import dev.kristof.spendly.common.exception.InvalidCredentialsException;
import dev.kristof.spendly.common.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 42L;
    private static final String EMAIL = "test@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String HASHED_PASSWORD = "hashedPassword";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // GET BY EMAIL

    @Test
    void getUserByEmail_shouldReturnExistingUser() {
        User user = mock(User.class);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(EMAIL);

        assertSame(user, result);

        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void getUserByEmail_shouldRejectUnknownEmail() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.getUserByEmail(EMAIL)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    // CREATE

    @Test
    void createUser_shouldSaveUserWithCorrectDataAndRole() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(
                DISPLAY_NAME,
                EMAIL,
                HASHED_PASSWORD
        );

        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());
        assertEquals(DISPLAY_NAME, result.getDisplayName());
        assertEquals(HASHED_PASSWORD, result.getPassword());
        assertEquals(UserRole.USER, result.getRole());

        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).save(result);
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail(EMAIL))
                .thenReturn(true);

        EmailAlreadyInUseException exception = assertThrows(
                EmailAlreadyInUseException.class,
                () -> userService.createUser(
                        DISPLAY_NAME,
                        EMAIL,
                        HASHED_PASSWORD
                )
        );

        assertEquals(
                "Email already in use: " + EMAIL,
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // GET BY ID

    @Test
    void getUserById_shouldReturnExistingUser() {
        User user = mock(User.class);

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(USER_ID);

        assertSame(user, result);

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getUserById_shouldRejectUnknownUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(USER_ID)
        );

        assertEquals(
                "User not found with id: " + USER_ID,
                exception.getMessage()
        );
    }

    // GET ALL USERS

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        List<User> expected = List.of(
                mock(User.class),
                mock(User.class)
        );

        when(userRepository.findAll()).thenReturn(expected);

        List<User> result = userService.getAllUsers();

        assertSame(expected, result);
        assertEquals(2, result.size());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList() {
        when(userRepository.findAll())
                .thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }
}
