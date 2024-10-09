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
    }
