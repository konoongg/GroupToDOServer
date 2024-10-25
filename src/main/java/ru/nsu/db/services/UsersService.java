    package ru.nsu.db.services;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Service;
    import ru.nsu.db.repositoris.UsersRepository;
    import ru.nsu.db.tables.Users;
    import ru.nsu.exceptions.UserAlreadyExistsException;

    import java.util.ArrayList;

    @Service
    public class UsersService implements UserDetailsService {

        @Autowired
        private BCryptPasswordEncoder passwordEncoder;

        @Autowired
        private UsersRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
            Users user = userRepository.findByLogin(login);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with username: " + login);
            }
            return new org.springframework.security.core.userdetails.User(
                    user.getLogin(),
                    user.getPassword(),
                    new ArrayList<>()
            );
        }

        public Users registerUser(Users user) {
            if (userRepository.findByLogin(user.getLogin()) != null) {
                throw new UserAlreadyExistsException("Login already exists: " + user.getLogin());
            }
            if (userRepository.findByEmail(user.getEmail()) != null) {
                throw new UserAlreadyExistsException("Email already exists: " + user.getEmail());
            }
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            return userRepository.save(user);
        }

        public Users findByLogin(String login) {
            return userRepository.findByLogin(login);
        }

        public void deleteUser(Users user) {
            userRepository.delete(user);
        }

        public boolean updatePassword(String username, String newPassword) {
            Users user = userRepository.findByLogin(username);
            if (user == null) {
                return false;
            }
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            userRepository.save(user);
            return true;
        }

        public boolean updateLogin(String username, String newLogin) {
            Users user = userRepository.findByLogin(username);
            if (user == null) {
                return false;
            }
            if (userRepository.findByLogin(newLogin) != null) {
                return false;
            }
            user.setLogin(newLogin);
            userRepository.save(user);
            return true;
        }

        public boolean updateEmail(String username, String newEmail) {
            Users user = userRepository.findByLogin(username);
            if (user == null) {
                return false;
            }
            if (userRepository.findByEmail(newEmail) != null) {
                return false;
            }
            user.setEmail(newEmail);
            userRepository.save(user);
            return true;
        }
    }
