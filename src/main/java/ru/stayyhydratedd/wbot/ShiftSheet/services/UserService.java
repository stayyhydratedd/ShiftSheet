package ru.stayyhydratedd.wbot.ShiftSheet.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.AuthUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.RegisterUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.mappers.UserMapper;
import ru.stayyhydratedd.wbot.ShiftSheet.models.RootFolder;
import ru.stayyhydratedd.wbot.ShiftSheet.models.User;
import ru.stayyhydratedd.wbot.ShiftSheet.repositories.UserRepository;
import ru.stayyhydratedd.wbot.ShiftSheet.security.MyUserDetails;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format("Пользователь с именем '%s' не найден", username)));

        return new MyUserDetails(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(int id){
        return userRepository.findByIdWithRootFolders(id);
    }

    public List<User> findAllByRootFoldersContaining(RootFolder rootFolder) {
        return userRepository.findAllByRootFoldersContaining(rootFolder);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveWithPasswordEncoding(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User saveWithoutPasswordEncoding(User user) {
        return userRepository.save(user);
    }

    public User registerUserDtoToUser(RegisterUserDTO registerUserDTO) {
        return userMapper.registerUserDtoToUser(registerUserDTO);
    }

    public User authUserDtoToUser(AuthUserDTO authUserDTO) {
        return userMapper.authUserDtoToUser(authUserDTO);
    }

    public AuthUserDTO registerUserDtoToAuthUserDto(RegisterUserDTO registerUserDTO) {
        return userMapper.registerUserDtoToAuthUserDto(registerUserDTO);
    }
}
