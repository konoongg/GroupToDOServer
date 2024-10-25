    package ru.nsu.db.services;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Service;
    import ru.nsu.db.repositoris.GroupsRepository;
    import ru.nsu.db.repositoris.UsersRepository;
    import ru.nsu.db.tables.Groups;
    import ru.nsu.db.tables.Users;
    import ru.nsu.db.tables.UsersInGroup;
    import ru.nsu.exceptions.UserAlreadyExistsException;

    import java.util.ArrayList;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Service
    public class UsersService implements UserDetailsService {

        @Autowired
        private BCryptPasswordEncoder passwordEncoder;

        @Autowired
        private UsersRepository userRepository;

        @Autowired
        private GroupsRepository groupsRepository;

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

        public List<Groups> getUserGroups(String username) {
            Users user = userRepository.findByLogin(username);
            if (user == null) {
                return null;
            }

            Set<UsersInGroup>  usersInGroup = user.getUsersInGroup();
            Set<Long> groupIds = new HashSet<>();
            for (UsersInGroup group : usersInGroup) {
                groupIds.add(group.getGroup().getId());
            }
            List<Groups> groups = groupsRepository.findAllById(groupIds);
            return groups;
        }
    }
